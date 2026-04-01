package com.tmccloud.aiapproval.config;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * RAG 配置类
 * 负责文档加载和初始化（LangChain4j + Spring AI 混合）
 */
@Configuration
public class RagConfig {

    /**
     * LangChain4j 嵌入模型（本地模型，无需 API）
     */
    @Bean
    public EmbeddingModel langchainEmbeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * LangChain4j 文档切片器
     */
    @Bean
    public DocumentSplitter langchainDocumentSplitter() {
        return DocumentSplitters.recursive(500, 100);
    }

    /**
     * Spring AI 文档切片器
     */
    @Bean
    public TextSplitter textSplitter() {
        return new TokenTextSplitter(1000, 200, 5, 10000, true);
    }

    /**
     * LangChain4j 内存向量存储（用于 ContentRetriever）
     */
    @Bean
    @Primary
    public EmbeddingStore<TextSegment> langchainEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * LangChain4j ContentRetriever（供 AiServiceConfig 使用）
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> langchainEmbeddingStore,
            EmbeddingModel langchainEmbeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(langchainEmbeddingStore)
                .embeddingModel(langchainEmbeddingModel)
                .maxResults(3)
                .minScore(0.7)
                .build();
    }

    /**
     * 应用启动时初始化向量库
     */
    @Bean
    @DependsOn({"vectorStore"})
    public ApplicationRunner vectorStoreInitializer() {
        return (ApplicationArguments args) -> {
            // 文档加载和向量化由 KnowledgeBaseService 处理
        };
    }
}
