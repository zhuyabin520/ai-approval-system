package com.tmccloud.aiapproval.config;

import com.tmccloud.aiapproval.service.ExpenseApprovalAssistant;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务配置类
 * 手动装配 AI Service，将检索器和 JSON Schema 注入
 */
@Configuration
public class AiServiceConfig {

    @Bean
    public ExpenseApprovalAssistant expenseApprovalAssistant(
            ChatLanguageModel chatLanguageModel,
            ContentRetriever contentRetriever,
            JsonSchema expenseCheckJsonSchema) {   // 注入JSON Schema

        return AiServices.builder(ExpenseApprovalAssistant.class)
                .chatLanguageModel(chatLanguageModel)
                .contentRetriever(contentRetriever)
                .chatLanguageModel(chatLanguageModel)  // 可以再次设置，但需要确保模型已配置response format
                .build();
    }
}