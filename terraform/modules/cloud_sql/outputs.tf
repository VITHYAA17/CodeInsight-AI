output "instance_name" {
  description = "The name of the database instance"
  value       = google_sql_database_instance.postgres.name
}

output "connection_name" {
  description = "Connection name for Cloud SQL Proxy / GKE Workload Identity"
  value       = google_sql_database_instance.postgres.connection_name
}

output "private_ip" {
  description = "Private IP address of PostgreSQL instance"
  value       = google_sql_database_instance.postgres.private_ip_address
}

output "database_name" {
  description = "Name of the created database"
  value       = google_sql_database.database.name
}

output "db_user" {
  description = "Database user"
  value       = google_sql_user.db_user.name
}
