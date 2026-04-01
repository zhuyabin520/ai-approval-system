package com.tmccloud.aiapproval.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MilvusVectorStoreConfig {

    @Value("${spring.ai.vectorstore.milvus.host:localhost}")
    private String host;

    @Value("${spring.ai.vectorstore.milvus.port:19530}")
    private int port;

    @Value("${spring.ai.vectorstore.milvus.collection-name:knowledge_base}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.milvus.embedding-dimension:1536}")
    private int embeddingDimension;

    @Value("${spring.ai.vectorstore.milvus.index-type:IVF_FLAT}")
    private String indexType;

    @Value("${spring.ai.vectorstore.milvus.metric-type:IP}")
    private String metricType;

    @Value("${spring.ai.vectorstore.milvus.database-name:default}")
    private String databaseName;

    @Bean
    public MilvusServiceClient milvusClient() {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                .build();
        return new MilvusServiceClient(connectParam);
    }

    @Bean
    @Primary
    public VectorStore vectorStore(MilvusServiceClient milvusClient, EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName(collectionName)
                .embeddingDimension(embeddingDimension)
                .initializeSchema(true)  // 重新创建集合
                .iDFieldName("id")
                .contentFieldName("content")
                .embeddingFieldName("embedding")
                .metadataFieldName("metadata")
                .metricType(MetricType.IP)  // 使用 IP
                .build();
    }
}