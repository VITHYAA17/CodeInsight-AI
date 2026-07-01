package com.codeinsight.backend.ai;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
public class LlmService {

    private final ChatClient chatClient;

    public LlmService(OpenAiChatClient openAiChatClient) {
        this.chatClient = openAiChatClient;
    }

    /**
     * Generate content using OpenAI GPT model
     * @param prompt The prompt to send to the model
     * @return The generated response
     */
    public String generateContent(String prompt) {
        try {
            Message message = new UserMessage(prompt);
            Prompt p = new Prompt(message, OpenAiChatOptions.builder()
                    .withModel("gpt-4-turbo")
                    .withTemperature(0.7)
                    .build());
            
            return chatClient.call(p).getResult().getOutput().getContent();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate content from LLM: " + e.getMessage(), e);
        }
    }

    /**
     * Generate content with custom temperature (creativity level)
     * @param prompt The prompt to send to the model
     * @param temperature Temperature between 0 (deterministic) and 2 (creative)
     * @return The generated response
     */
    public String generateContent(String prompt, double temperature) {
        try {
            Message message = new UserMessage(prompt);
            Prompt p = new Prompt(message, OpenAiChatOptions.builder()
                    .withModel("gpt-4-turbo")
                    .withTemperature(temperature)
                    .build());
            
            return chatClient.call(p).getResult().getOutput().getContent();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate content from LLM: " + e.getMessage(), e);
        }
    }

    /**
     * Generate structured JSON response
     * @param prompt The prompt requesting JSON output
     * @return JSON string from the model
     */
    public String generateJsonContent(String prompt) {
        String promptWithJsonRequest = prompt + "\n\nReturn ONLY valid JSON, no markdown, no explanations.";
        return generateContent(promptWithJsonRequest, 0.2); // Lower temperature for consistency
    }

    /**
     * Stream content generation (for long responses)
     * @param prompt The prompt to send to the model
     * @return The generated response
     */
    public String generateStreamContent(String prompt) {
        return generateContent(prompt);
    }

    /**
     * Analyze and summarize content
     * @param content Content to analyze
     * @param analysisType Type of analysis (summary, gaps, strengths, etc.)
     * @return Analysis result
     */
    public String analyzeContent(String content, String analysisType) {
        String analysisPrompt = """
                Analyze the following content and provide a %s:
                
                CONTENT:
                %s
                
                Be concise and specific.
                """.formatted(analysisType, content);
        
        return generateContent(analysisPrompt);
    }
}
