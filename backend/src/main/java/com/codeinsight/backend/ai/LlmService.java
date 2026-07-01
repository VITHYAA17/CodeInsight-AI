package com.codeinsight.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class LlmService {

    private final String apiKey;
    private final String model = "gpt-4-turbo";
    private final String baseUrl = "https://api.openai.com/v1";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LlmService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        // Get API key from environment variable
        this.apiKey = System.getenv("OPENAI_API_KEY");
        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set");
        }
    }

    /**
     * Generate content using OpenAI API with default temperature (0.7)
     */
    public String generateContent(String prompt) {
        return generateContent(prompt, 0.7);
    }

    /**
     * Generate content with custom temperature
     */
    public String generateContent(String prompt, double temperature) {
        try {
            Map<String, Object> request = createRequest(prompt, temperature);
            String response = callOpenAiApi(request);
            return extractContent(response);
        } catch (Exception e) {
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
        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

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
