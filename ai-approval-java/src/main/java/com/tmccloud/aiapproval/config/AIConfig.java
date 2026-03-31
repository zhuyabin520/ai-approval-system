package com.tmccloud.aiapproval.config;

import com.tmccloud.aiapproval.entity.AISettings;
import com.tmccloud.aiapproval.service.AISettingsService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class AIConfig {

    @Autowired
    private AISettingsService aiSettingsService;
    
    @Value("${langchain4j.open-ai.chat-model.api-key}")
    private String fallbackApiKey;

    @Value("${langchain4j.open-ai.chat-model.base-url}")
    private String fallbackBaseUrl;

    @Value("${langchain4j.open-ai.chat-model.model-name}")
    private String fallbackModelName;

    @Value("${langchain4j.open-ai.chat-model.temperature}")
    private double fallbackTemperature;

    @Value("${langchain4j.open-ai.chat-model.max-tokens}")
    private int fallbackMaxTokens;
    
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // 从数据库获取AI设置
        AISettings settings = aiSettingsService.getCurrentSettings();
        
        // 如果数据库中没有设置，使用配置文件中的默认值
        String apiKey = (settings != null && settings.getApiKey() != null) ? settings.getApiKey() : fallbackApiKey;
        String baseUrl = (settings != null && settings.getBaseUrl() != null) ? settings.getBaseUrl() : fallbackBaseUrl;
        String modelName = (settings != null && settings.getModelName() != null) ? settings.getModelName() : fallbackModelName;
        double temperature = (settings != null && settings.getTemperature() != null) ? settings.getTemperature().doubleValue() : fallbackTemperature;
        int maxTokens = (settings != null && settings.getMaxTokens() != null) ? settings.getMaxTokens() : fallbackMaxTokens;
        
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