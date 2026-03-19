package com.decisionagent.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * LLM Service
 *
 * Service for interacting with Large Language Models using LangChain4j.
 * Supports OpenAI-compatible APIs (OpenAI, 智谱AI, etc.)
 *
 * @author DecisionAgent Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class LlmService {

    private final ChatLanguageModel chatModel;

    /**
     * Constructor with configuration from application.yml
     *
     * @param apiKey API key for the LLM provider
     * @param baseUrl Base URL for the LLM API (optional, for custom endpoints)
     * @param modelName Model name (e.g., gpt-4, glm-4-flash, etc.)
     * @param temperature Temperature for response randomness (0.0-1.0)
     * @param maxTokens Maximum tokens in response
     * @param timeout Request timeout in seconds
     */
    public LlmService(
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.base-url:}") String baseUrl,
            @Value("${llm.model-name:gpt-4o-mini}") String modelName,
            @Value("${llm.temperature:0.7}") Double temperature,
            @Value("${llm.max-tokens:2000}") Integer maxTokens,
            @Value("${llm.timeout:60}") Integer timeout) {

        OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .timeout(Duration.ofSeconds(timeout));

        // Set custom base URL if provided (for 智谱AI or other compatible APIs)
        if (baseUrl != null && !baseUrl.isEmpty()) {
            builder.baseUrl(baseUrl);
            log.info("Using custom base URL: {}", baseUrl);
        }

        this.chatModel = builder.build();
        log.info("LLM Service initialized with model: {}, provider: {}",
                modelName, baseUrl.isEmpty() ? "OpenAI" : baseUrl);
    }

    /**
     * Send a prompt to the LLM and get the response
     *
     * @param prompt The input prompt
     * @return LLM response
     */
    public String ask(String prompt) {
        log.debug("Sending prompt to LLM, length: {}", prompt.length());

        try {
            String response = chatModel.generate(prompt);
            log.debug("Received response from LLM, length: {}", response.length());
            return response;
        } catch (Exception e) {
            log.error("Error calling LLM", e);
            return "抱歉，AI分析服务暂时不可用。请稍后重试。\n\n错误信息：" + e.getMessage();
        }
    }

    /**
     * Send a prompt with system message to the LLM
     *
     * @param systemMessage System message for context
     * @param userMessage User message
     * @return LLM response
     */
    public String ask(String systemMessage, String userMessage) {
        String fullPrompt = String.format("System: %s\n\nUser: %s", systemMessage, userMessage);
        return ask(fullPrompt);
    }
}
