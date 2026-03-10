package com.tmccloud.aiapproval.config;



import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AIConfig {

    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.temperature}")
    private double temperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens}")
    private int maxTokens;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)          // 千问API Key
                .baseUrl(baseUrl)        // 千问兼容OpenAI的接口地址
                .modelName(modelName)    // 千问模型名称
                .temperature(temperature) // 随机性
                .maxTokens(maxTokens)    // 最大生成token
                .timeout(Duration.ofMinutes(1)) // 超时时间
                .build();
    }
}