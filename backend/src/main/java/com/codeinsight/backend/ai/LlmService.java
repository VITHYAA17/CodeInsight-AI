package com.codeinsight.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final String apiKey;
    private final String model = "gpt-4o-mini";
    private final String baseUrl = "https://api.openai.com/v1";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LlmService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        // Get API key from environment variable
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            log.warn("OPENAI_API_KEY environment variable is not set. OpenAI calls will fail at runtime.");
        }
    }

    /**
     * Generate content using OpenAI API with default temperature (0.7)
     */
    public String generateContent(String prompt) {
        return generateContent(prompt, 0.8);
    }

    /**
     * Generate content with custom temperature
     */
    public String generateContent(String prompt, double temperature) {
        try {
            log.info("Initiating request to OpenAI completions API using model {}...", model);
            Map<String, Object> request = createRequest(prompt, temperature);
            String response = callOpenAiApi(request);
            log.info("Successfully received response from OpenAI completions API.");
            return extractContent(response);
        } catch (Exception e) {
            log.error("Exception during OpenAI API call: ", e);
            throw new RuntimeException("Failed to generate content: " + e.getMessage(), e);
        }
    }

    /**
     * Generate JSON-formatted content
     */
    public String generateJsonContent(String prompt) {
        String promptWithJson = prompt + "\n\nReturn ONLY valid JSON, no markdown, no explanations.";
        return generateContent(promptWithJson, 0.2);
    }

    /**
     * Analyze content
     */
    public String analyzeContent(String content, String analysisType) {
        String prompt = "Please analyze the following " + analysisType + ":\n\n" + content;
        return generateContent(prompt, 0.5);
    }

    /**
     * Stream content chunk by chunk from OpenAI completions API
     */
    public void streamChat(String prompt, double temperature, java.util.function.Consumer<String> onChunk, Runnable onComplete, java.util.function.Consumer<Throwable> onError) {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            onError.accept(new IllegalStateException("OPENAI_API_KEY environment variable is not set"));
            return;
        }

        try {
            java.net.URL url = new java.net.URI(baseUrl + "/chat/completions").toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            Map<String, Object> request = createRequest(prompt, temperature);
            request.put("stream", true);
            String jsonBody = objectMapper.writeValueAsString(request);

            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                try (java.io.InputStream err = conn.getErrorStream()) {
                    String errText = err != null ? new String(err.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8) : "HTTP " + status;
                    onError.accept(new RuntimeException("OpenAI error: " + errText));
                    return;
                }
            }

            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith(":")) continue;
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            Map<String, Object> map = objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {});
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> choices = (List<Map<String, Object>>) map.get("choices");
                            if (choices != null && !choices.isEmpty()) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> delta = (Map<String, Object>) choices.get(0).get("delta");
                                if (delta != null && delta.containsKey("content")) {
                                    String chunk = (String) delta.get("content");
                                    if (chunk != null) {
                                        onChunk.accept(chunk);
                                    }
                                }
                            }
                        } catch (Exception parseEx) {
                            log.debug("Skipping unparseable SSE line: {}", line);
                        }
                    }
                }
            }
            onComplete.run();
        } catch (Exception e) {
            log.error("Streaming from OpenAI failed: ", e);
            onError.accept(e);
        }
    }

    /**
     * Stream content (returns same as regular for now)
     */
    public String generateStreamContent(String prompt) {
        return generateContent(prompt);
    }

    /**
     * Create OpenAI API request
     */
    private Map<String, Object> createRequest(String prompt, double temperature) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("temperature", temperature);
        request.put("max_tokens", 2000);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        request.put("messages", messages);
        return request;
    }

    /**
     * Call OpenAI API
     */
    private String callOpenAiApi(Map<String, Object> request) throws Exception {
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set");
        }
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String jsonRequest = objectMapper.writeValueAsString(request);
        HttpEntity<String> entity = new HttpEntity<>(jsonRequest, headers);

        return restTemplate.postForObject(url, entity, String.class);
    }

    /**
     * Extract content from OpenAI response
     */
    private String extractContent(String response) throws Exception {
        Map<String, Object> responseMap = objectMapper.readValue(
            response, new TypeReference<Map<String, Object>>() { });

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

        if (choices != null && !choices.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message != null) {
                return (String) message.get("content");
            }
        }

        throw new RuntimeException("Invalid response format from OpenAI");
    }
}
