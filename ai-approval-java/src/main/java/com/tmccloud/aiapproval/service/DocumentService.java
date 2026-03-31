package com.tmccloud.aiapproval.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    @Value("${langchain4j.open-ai.embedding-model.api-key}")
    private String apiKey;

    @Value("${langchain4j.open-ai.embedding-model.base-url}")
    private String baseUrl;

    @Value("${langchain4j.open-ai.embedding-model.model-name}")
    private String modelName;

    /**
     * 加载并存储报销管理制度文档
     * @param filePath 文档文件路径
     */
    public void loadAndStoreDocument(String filePath) {
        try (java.io.FileInputStream inputStream = new java.io.FileInputStream(filePath)) {
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(inputStream);

            // 分割文档为段落
            List<TextSegment> segments = DocumentSplitters.recursive(1000, 200).split(document);

            // 创建嵌入模型（使用千问 text-embedding-v1 模型）
            EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl)
                    .modelName(modelName)
                    .build();

            // 生成嵌入并存储
            for (TextSegment segment : segments) {
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);
            }
        } catch (java.io.FileNotFoundException e) {
            throw new RuntimeException("文件未找到：" + filePath, e);
        } catch (Exception e) {
            throw new RuntimeException("加载文档失败：" + e.getMessage(), e);
        }
    }


    /**
     * 检索相关的制度条款
     * @param query 查询文本
     * @return 相关条款列表
     */
    public List<TextSegment> searchRelevantClauses(String query) {
        // 创建嵌入模型
        EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName("text-embedding-v1")
                .build();

        // 生成查询嵌入
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 检索相关文档片段
        // 检索相关文档片段
        return embeddingStore.findRelevant(queryEmbedding, 3)
                .stream()
                .map(dev.langchain4j.store.embedding.EmbeddingMatch::embedded)
                .toList();
    }
}
