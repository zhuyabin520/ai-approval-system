package com.tmccloud.aiapproval.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingOptions;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    @Value("${spring.ai.dashscope.embedding.model}")
    private String embeddingModel;
    @Value("${spring.ai.dashscope.chat.options.model}")
    private String chatModel;
    @Value("${spring.ai.vectorstore.milvus.host:localhost}")
    private String milvusHost;

    @Value("${spring.ai.vectorstore.milvus.port:19530}")
    private int milvusPort;

    @Value("${spring.ai.milvus.collection-name:knowledge_base}")
    private String collectionName;
    @Bean
    @Primary
    public EmbeddingModel embeddingModel() {
        // 创建 DashScopeApi
        DashScopeApi dashScopeApi = new DashScopeApi(apiKey);

        // 使用 MetadataMode.EMBED，并指定模型
        DashScopeEmbeddingOptions options = DashScopeEmbeddingOptions.builder()
                .withModel(embeddingModel)
                .build();

        // 返回 EmbeddingModel
        return new DashScopeEmbeddingModel(dashScopeApi, MetadataMode.EMBED, options);
    }
    @Bean
    public ChatClient chatClient() {
        // 使用支持 baseUrl 的构造函数
        DashScopeApi dashScopeApi = new DashScopeApi(apiKey);

        // 构建选项 - 确保不设置多模态相关参数
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .withModel(chatModel)
                // 重要：显式设置多模态为 false
                .withMultiModel(false)
                .build();

        DashScopeChatModel chatModelInstance = new DashScopeChatModel(dashScopeApi, options);

        return ChatClient.builder(chatModelInstance).build();
    }
    @Bean
    public MilvusClientV2 milvusClientV2() {
        ConnectConfig config = ConnectConfig.builder()
                .uri(String.format("http://%s:%d", milvusHost, milvusPort))
                .build();
        return new MilvusClientV2(config);
    }

}