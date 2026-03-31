package com.tmccloud.aiapproval.config;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.util.List;

/**
 * RAG 配置类
 * 负责文档加载、切片、向量化和检索器配置[citation:3][citation:7]
 */
@Slf4j
@Configuration
public class RagConfig {

    @Value("${app.knowledge-base-path}")
    private String knowledgeBasePath;

    /**
     * 嵌入模型：将文本转为向量
     * 使用轻量级本地模型，也可替换为DeepSeek的嵌入模型
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        // 使用本地轻量级模型，无需API调用
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * 向量存储：存储文档片段的向量表示
     * 使用内存存储，重启后数据丢失。生产环境可替换为PGVector、Chroma等[citation:7]
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * 文档切片器配置
     * 采用递归切片，最大500字符，重叠100字符，保证上下文连贯[citation:3][citation:5]
     */
    @Bean
    public DocumentSplitter documentSplitter() {
        return DocumentSplitters.recursive(500, 100);
    }

    /**
     * 文档摄入器：将原始文档切片、向量化并存入向量库
     */
    @Bean
    public EmbeddingStoreIngestor embeddingStoreIngestor(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            DocumentSplitter documentSplitter) {
        return EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
    }

    /**
     * 内容检索器：根据用户问题检索相关文档片段
     * 配置最大返回结果数和最小相似度阈值[citation:3]
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)           // 最多返回3个相关片段
                .minScore(0.7)            // 相似度阈值0.7
                .build();
    }

    /**
     * 应用启动时加载知识库文档
     * 使用 ApplicationRunner 确保所有 Bean 初始化完成后再执行
     */
    @Bean
    public ApplicationRunner knowledgeBaseLoader(EmbeddingStoreIngestor ingestor) {
        return (ApplicationArguments args) -> {
            try {
                log.info("开始加载知识库文档，路径：{}", knowledgeBasePath);
                // 加载指定目录下的所有.txt文件
                List<Document> documents = FileSystemDocumentLoader.loadDocuments(
                        Paths.get(knowledgeBasePath),
                        new TextDocumentParser()
                );

                log.info("加载了 {} 个文档", documents.size());

                // 将文档摄入向量库
                ingestor.ingest(documents);

                log.info("知识库加载完成");
            } catch (Exception e) {
                log.error("加载知识库失败", e);
            }
        };
    }
}