package com.codeinsight.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URI;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		configureDatabaseEnvironment();
		SpringApplication.run(BackendApplication.class, args);
	}

	/**
	 * Automatically adapts cloud-provided database URLs (Render, Railway, Heroku, Neon)
	 * from postgres:// or postgresql:// format to standard Spring Boot JDBC format.
	 */
	private static void configureDatabaseEnvironment() {
		String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
		if (dbUrl == null || dbUrl.isEmpty()) {
			dbUrl = System.getenv("DATABASE_URL");
		}

		if (dbUrl != null && !dbUrl.isEmpty()) {
			// If URL contains a Render external domain, check if internal DNS resolves (same region)
			if (System.getenv("RENDER") != null && dbUrl.contains(".render.com")) {
				java.util.regex.Matcher m = java.util.regex.Pattern.compile("(dpg-[a-zA-Z0-9]+(-[a-zA-Z0-9]+)?)\\.[a-zA-Z0-9.\\-]*render\\.com").matcher(dbUrl);
				if (m.find()) {
					String internalHost = m.group(1);
					try {
						java.net.InetAddress.getByName(internalHost);
						dbUrl = m.replaceFirst(internalHost);
						System.out.println("[DATABASE] Internal DNS resolved for " + internalHost + " - using private Render network.");
					} catch (Exception e) {
						System.out.println("[DATABASE] Internal DNS not reachable for " + internalHost + " (cross-region or not ready) - keeping host as provided.");
					}
				}
			}

			if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
				try {
					String uriString = dbUrl.startsWith("postgres://")
							? "postgresql://" + dbUrl.substring("postgres://".length())
							: dbUrl;
					URI uri = new URI(uriString);

					String host = uri.getHost();
					int port = uri.getPort() > 0 ? uri.getPort() : 5432;
					String path = uri.getPath();

					String userInfo = uri.getUserInfo();
					if (userInfo != null) {
						String[] parts = userInfo.split(":", 2);
						String username = parts[0];
						String password = parts.length > 1 ? parts[1] : "";
						System.setProperty("spring.datasource.username", username);
						System.setProperty("spring.datasource.password", password);
					}

					String query = uri.getQuery();
					StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
							.append(host)
							.append(":")
							.append(port)
							.append(path);

					if (query != null && !query.isEmpty()) {
						jdbcUrl.append("?").append(query);
					}

					System.setProperty("spring.datasource.url", jdbcUrl.toString());
				} catch (Exception e) {
					System.err.println("Failed to parse DATABASE_URL: " + e.getMessage());
				}
			} else if (!dbUrl.startsWith("jdbc:")) {
				System.setProperty("spring.datasource.url", "jdbc:" + dbUrl);
			} else {
				System.setProperty("spring.datasource.url", dbUrl);
			}

			System.out.println("[DATABASE] Configured DataSource URL: " + System.getProperty("spring.datasource.url"));
		}
	}

}
