package com.tmccloud.aiapproval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tmccloud.aiapproval.entity.KnowledgeBase;
import com.tmccloud.aiapproval.mapper.KnowledgeBaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KnowledgeBaseService {

    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private DocumentChunkingService documentChunkingService;

    @Autowired
    private VectorStore vectorStore;  // Spring AI 会自动配置

    @Value("${knowledgebase.storage.path}")
    private String storagePath;

    @Value("${knowledgebase.chunk.size:800}")
    private int chunkSize;

    @Value("${knowledgebase.chunk.overlap:200}")
    private int chunkOverlap;

    /**
     * 上传并向量化文档
     */
    public KnowledgeBase uploadDocument(MultipartFile file, String description,
                                        Long userId, String tags) throws IOException {
        Path filePath = saveFile(file);
        KnowledgeBase knowledgeBase = createKnowledgeBaseRecord(file, description, userId, tags, filePath);
        asyncProcessDocument(knowledgeBase, filePath);
        return knowledgeBase;
    }

    private Path saveFile(MultipartFile file) throws IOException {
        Path storageDir = Paths.get(storagePath);
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf('.')) : ".tmp";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = storageDir.resolve(uniqueFilename);

        file.transferTo(filePath.toFile());
        return filePath;
    }

    private KnowledgeBase createKnowledgeBaseRecord(MultipartFile file, String description,
                                                    Long userId, String tags, Path filePath) {
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setName(file.getOriginalFilename());
        knowledgeBase.setDescription(description);
        knowledgeBase.setDocumentPath(filePath.toString());
        knowledgeBase.setDocumentType(file.getContentType());
        knowledgeBase.setFileSize(file.getSize());
        knowledgeBase.setUploadTime(LocalDateTime.now());
        knowledgeBase.setStatus("UPLOADED");
        knowledgeBase.setUserId(userId);
        knowledgeBase.setTags(tags);

        knowledgeBaseMapper.insert(knowledgeBase);
        return knowledgeBase;
    }
    /**
     * 同步处理文档（用于调试）
     */
    public KnowledgeBase uploadDocumentSync(MultipartFile file, String description,
                                            Long userId, String tags) throws Exception {
        log.info("=== 开始同步处理文档 ===");

        Path filePath = saveFile(file);
        KnowledgeBase knowledgeBase = createKnowledgeBaseRecord(file, description, userId, tags, filePath);

        try {
            // 1. 解析
            List<Document> parsedDocuments = documentParserService.parseDocument(
                    filePath.toFile(), knowledgeBase.getDocumentType());

            if (parsedDocuments.isEmpty()) {
                throw new RuntimeException("文档解析失败");
            }

            enrichDocumentsMetadata(parsedDocuments, knowledgeBase);

            // 2. 切片
            List<Document> chunkedDocuments = documentChunkingService.chunkDocument(
                    parsedDocuments, chunkSize, chunkOverlap);

            addChunkMetadata(chunkedDocuments, knowledgeBase);

            // 3. 向量化 - 这一步会直接抛出异常
            log.info("准备向量化，文档块数量: {}", chunkedDocuments.size());

            // 打印第一个文档块的内容
            if (!chunkedDocuments.isEmpty()) {
                log.info("第一个文档块内容: {}",
                        chunkedDocuments.get(0).getText().substring(0, Math.min(200, chunkedDocuments.get(0).getText().length())));
            }

            vectorStore.add(chunkedDocuments);

            // 4. 更新状态
            knowledgeBase.setStatus("VECTORIZED");
            knowledgeBase.setVectorizationTime(LocalDateTime.now());
            knowledgeBase.setChunkCount(chunkedDocuments.size());
            knowledgeBaseMapper.updateStatus(knowledgeBase);

            log.info("=== 同步处理成功 ===");

        } catch (Exception e) {
            log.error("=== 同步处理失败 ===", e);
            knowledgeBase.setStatus("ERROR");
            knowledgeBase.setErrorMessage(e.getMessage());
            knowledgeBaseMapper.updateStatus(knowledgeBase);
            throw e;
        }

        return knowledgeBase;
    }

    private void asyncProcessDocument(KnowledgeBase knowledgeBase, Path filePath) {
        CompletableFuture.runAsync(() -> {
            try {
                log.info("========== 开始处理文档 ==========");

                // 1. 解析文档
                List<Document> parsedDocuments = documentParserService.parseDocument(
                        filePath.toFile(), knowledgeBase.getDocumentType());

                if (parsedDocuments.isEmpty()) {
                    updateStatus(knowledgeBase.getId(), "ERROR", "文档解析失败");
                    return;
                }

                // 2. 切片文档
                List<Document> chunkedDocuments = documentChunkingService.chunkDocument(
                        parsedDocuments, chunkSize, chunkOverlap);

                log.info("切片完成，共 {} 个文档块", chunkedDocuments.size());

                // 3. 构建最终的 Document 列表
                List<Document> finalDocuments = new ArrayList<>();
                for (int i = 0; i < chunkedDocuments.size(); i++) {
                    Document chunk = chunkedDocuments.get(i);

                    // 生成唯一 ID
                    String uniqueId = UUID.randomUUID().toString();

                    // 获取内容
                    String content = chunk.getText();
                    if (content == null) content = "";

                    // 构建业务元数据 - 所有业务数据都放在这里
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("knowledge_base_id", knowledgeBase.getId() != null ? knowledgeBase.getId() : 0L);
                    metadata.put("document_name", safeString(knowledgeBase.getName()));
                    metadata.put("document_type", safeString(knowledgeBase.getDocumentType()));
                    metadata.put("tags", safeString(knowledgeBase.getTags()));
                    metadata.put("user_id", knowledgeBase.getUserId() != null ? knowledgeBase.getUserId() : 0L);
                    metadata.put("upload_time", knowledgeBase.getUploadTime() != null ?
                            knowledgeBase.getUploadTime().toString() : LocalDateTime.now().toString());
                    metadata.put("chunk_index", i);
                    metadata.put("total_chunks", chunkedDocuments.size());
                    metadata.put("chunk_size", content.length());

                    // 添加原有的元数据
                    for (Map.Entry<String, Object> entry : chunk.getMetadata().entrySet()) {
                        if (entry.getValue() != null) {
                            metadata.put(entry.getKey(), entry.getValue());
                        }
                    }
                    Gson gson = new Gson();
                    JsonObject metadataJson = gson.toJsonTree(metadata).getAsJsonObject();

                    Document docWithId = new Document(uniqueId, content, metadata);
                    finalDocuments.add(docWithId);

                    log.debug("创建文档块 {} - ID: {}, 内容长度: {}", i, uniqueId, content.length());
                }

                // 4. 验证所有字段
                for (Document doc : finalDocuments) {
                    if (doc.getId() == null || doc.getId().isEmpty()) {
                        throw new RuntimeException("Document ID 为空");
                    }
                    if (doc.getText() == null) {
                        throw new RuntimeException("Document 内容为 null");
                    }
                    for (Map.Entry<String, Object> entry : doc.getMetadata().entrySet()) {
                        if (entry.getValue() == null) {
                            log.warn("元数据字段 {} 为 null，设置默认值", entry.getKey());
                            doc.getMetadata().put(entry.getKey(), "");
                        }
                    }
                }

                // 5. 插入向量库
                 log.info("开始向量化存储，共 {} 个文档", finalDocuments.size());
                vectorStore.add(finalDocuments);
                log.info("向量化存储成功");

                // 6. 更新状态
                knowledgeBase.setStatus("VECTORIZED");
                knowledgeBase.setVectorizationTime(LocalDateTime.now());
                knowledgeBase.setChunkCount(finalDocuments.size());
                knowledgeBaseMapper.updateStatus(knowledgeBase);

                log.info("文档处理成功: {}, 共 {} 个向量", knowledgeBase.getName(), finalDocuments.size());

            } catch (Exception e) {
                log.error("文档处理失败: {}", knowledgeBase.getName(), e);
                updateStatus(knowledgeBase.getId(), "ERROR", e.getMessage());
            }
        });
    }

   

    /**
     * 标准化键名：驼峰转下划线
     */
    private String normalizeKey(String key) {
        // 处理特殊映射
        switch (key) {
            case "knowledgeBaseId": return "knowledge_base_id";
            case "knowledgeBaseName": return "document_name";
            case "knowledgeBaseTags": return "tags";
            case "userId": return "user_id";
            case "uploadTime": return "upload_time";
            case "documentName": return "document_name";
            case "documentType": return "document_type";
            case "chunkIndex": return "chunk_index";
            case "totalChunks": return "total_chunks";
            case "chunkSize": return "chunk_size";
            default: return key;
        }
    }


    /**
     * 验证 Document 符合 Milvus 要求
     */
    private void validateDocumentForMilvus(Document doc) {
        // 验证 ID
        if (doc.getId() == null || doc.getId().isEmpty()) {
            throw new IllegalArgumentException("Document ID 不能为空");
        }

        // 验证内容
        if (doc.getText() == null) {
            throw new IllegalArgumentException("Document 内容不能为 null");
        }

        // 验证元数据中所有值都不为 null
        for (Map.Entry<String, Object> entry : doc.getMetadata().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                throw new IllegalArgumentException(
                        String.format("文档 %s 的元数据键 '%s' 值为 null", doc.getId(), key));
            }

            // 特别检查字符串字段
            if (value instanceof String) {
                // 空字符串是允许的
                log.trace("字符串字段 {} = '{}'", key, value);
            }
        }

        log.info("文档验证通过: ID={}, 内容长度={}, 元数据字段={}",
                doc.getId(), doc.getText().length(), doc.getMetadata().keySet());
    }
    /**
     * 安全转换字符串，null 转为空字符串
     */
    private String safeString(String value) {
        return value != null ? value : "";
    }

    /**
     * 安全转换 Long，null 转为 0L
     */
    private Long safeLong(Long value) {
        return value != null ? value : 0L;
    }

    /**
     * 验证 Document 的所有字段
     */
    private void validateDocumentFields(Document doc) {
        // 验证 ID
        if (doc.getId() == null || doc.getId().isEmpty()) {
            throw new IllegalArgumentException("Document ID 不能为 null 或空");
        }

        // 验证内容
        if (doc.getText() == null) {
            throw new IllegalArgumentException("Document 内容不能为 null");
        }

        // 验证元数据中的所有字符串字段
        for (Map.Entry<String, Object> entry : doc.getMetadata().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                throw new IllegalArgumentException(
                        String.format("文档 %s 的元数据键 '%s' 值为 null", doc.getId(), key));
            }

            // 如果是字符串，检查是否为 null（已通过上面检查）
            if (value instanceof String) {
                // 空字符串是允许的
                log.trace("字符串字段 {} = '{}'", key, value);
            }
        }

        log.debug("文档验证通过: ID={}, 内容长度={}, 元数据字段数={}",
                doc.getId(), doc.getText().length(), doc.getMetadata().size());
    }

    private void enrichDocumentsMetadata(List<Document> documents, KnowledgeBase knowledgeBase) {
        for (Document doc : documents) {
            // 使用与集合 schema 一致的字段名
            doc.getMetadata().put("knowledge_base_id", safeLong(knowledgeBase.getId()));
            doc.getMetadata().put("document_name", safeString(knowledgeBase.getName()));
            doc.getMetadata().put("document_type", safeString(knowledgeBase.getDocumentType()));
            doc.getMetadata().put("tags", safeString(knowledgeBase.getTags()));
            doc.getMetadata().put("user_id", safeLong(knowledgeBase.getUserId()));
            doc.getMetadata().put("upload_time", knowledgeBase.getUploadTime() != null ?
                    knowledgeBase.getUploadTime().toString() : LocalDateTime.now().toString());
        }
    }


    // ... existing code ...

    private void addChunkMetadata(List<Document> chunks, KnowledgeBase knowledgeBase) {
        // 创建新的 List 来存放带 ID 的 Document
        List<Document> documentsWithId = new ArrayList<>();

        // 确保 knowledgeBase.getId() 不为 null
        Long kbId = knowledgeBase.getId() != null ? knowledgeBase.getId() : 0L;

        for (int i = 0; i < chunks.size(); i++) {
            Document originalChunk = chunks.get(i);

            // 生成唯一 ID
            String uniqueId = String.format("kb_%d_chunk_%d_%s",
                    kbId,
                    i,
                    UUID.randomUUID().toString().replace("-", "").substring(0, 8));

            // 获取原有元数据并添加新元数据
            Map<String, Object> metadata = new HashMap<>(originalChunk.getMetadata());
            metadata.put("knowledgeBaseId", kbId);
            metadata.put("knowledgeBaseName", safeString(knowledgeBase.getName()));
            metadata.put("chunkIndex", i);
            metadata.put("totalChunks", chunks.size());
            metadata.put("chunkSize", originalChunk.getText().length());
            metadata.put("documentType", safeString(knowledgeBase.getDocumentType()));
            metadata.put("userId", knowledgeBase.getUserId() != null ? knowledgeBase.getUserId() : 0L);
            metadata.put("tags", safeString(knowledgeBase.getTags()));
            metadata.put("uploadTime", knowledgeBase.getUploadTime() != null ?
                    knowledgeBase.getUploadTime().toString() : LocalDateTime.now().toString());
            // 将业务元数据序列化为 JSON 字符串
            String metadataJson = toJsonString(metadata);

            // 创建 Document，将 JSON 字符串作为 metadata 字段的值
            // 注意：这里 metadata 是 Map，我们需要把 JSON 字符串放进去，但 MilvusVectorStore 可能会再次序列化，
            // 所以最好直接使用一个特殊的键，比如 "__metadata_json"。但为了简单，我们直接放一个键值对。
            Map<String, Object> finalMetadata = new HashMap<>();
            finalMetadata.put("data", metadataJson);  // 将 JSON 字符串存入一个键

            Document docWithId = new Document(uniqueId, originalChunk.getText(), finalMetadata);
            documentsWithId.add(docWithId);

            log.debug("创建文档块 {} - ID: {}, 内容长度：{}", i, uniqueId, originalChunk.getText().length());
        }

        // 更新 chunks 列表
        chunks.clear();
        chunks.addAll(documentsWithId);
    }
    private String toJsonString(Map<String, Object> map) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("JSON 转换失败", e);
            return "{}";
        }
    }

    private void updateStatus(Long id, String status, String errorMessage) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(id);
        kb.setStatus(status);
        if (errorMessage != null && errorMessage.length() > 500) {
            errorMessage = errorMessage.substring(0, 500);
        }
        kb.setErrorMessage(errorMessage);
        knowledgeBaseMapper.updateStatus(kb);
    }

    // ==================== 搜索方法 ====================

    /**
     * 基础相似性搜索
     */
    public List<Document> searchSimilar(String query, int topK) {
        // 使用默认方法
        return vectorStore.similaritySearch(query);
    }

    /**
     * 带 topK 的搜索
     */
    public List<Document> searchSimilarWithTopK(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        return vectorStore.similaritySearch(request);
    }

    /**
     * 带相似度阈值的搜索
     */
    public List<Document> searchSimilarWithThreshold(String query, int topK, double similarityThreshold) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();
        return vectorStore.similaritySearch(request);
    }

    /**
     * 带过滤条件的搜索
     * 过滤表达式格式取决于具体的 VectorStore 实现（如 Milvus）
     */
    public List<Document> searchWithFilter(String query, int topK, String filterExpression) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(filterExpression)
                .build();
        return vectorStore.similaritySearch(request);
    }

    /**
     * 按知识库ID过滤搜索
     */
    public List<Document> searchByKnowledgeBase(String query, Long knowledgeBaseId, int topK) {
        // 使用过滤表达式
        // 使用 JSON 表达式过滤
        String filterExpression = String.format("metadata['knowledge_base_id'] == %d", knowledgeBaseId);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(filterExpression)
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * 带阈值的知识库搜索
     */
    public List<Document> searchByKnowledgeBaseWithThreshold(
            String query, Long knowledgeBaseId, int topK, double similarityThreshold) {

        String filterExpression = String.format("metadata['knowledgeBaseId'] == %d", knowledgeBaseId);

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(filterExpression)
                .build();

        return vectorStore.similaritySearch(request);
    }

    /**
     * 多条件过滤搜索
     */
    public List<Document> searchWithMultipleFilters(
            String query, int topK, Long knowledgeBaseId, String tags, Double minSimilarity) {

        List<String> conditions = new ArrayList<>();

        if (knowledgeBaseId != null) {
            conditions.add(String.format("metadata['knowledgeBaseId'] == %d", knowledgeBaseId));
        }
        if (tags != null && !tags.isEmpty()) {
            conditions.add(String.format("metadata['tags'] contains '%s'", tags));
        }

        String filterExpression = conditions.isEmpty() ? null :
                String.join(" && ", conditions);

        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(topK);

        if (minSimilarity != null) {
            builder.similarityThreshold(minSimilarity);
        }
        if (filterExpression != null) {
            builder.filterExpression(filterExpression);
        }

        return vectorStore.similaritySearch(builder.build());
    }

    /**
     * 获取搜索结果并返回自定义对象
     */
    public List<SearchResult> searchWithScores(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        List<Document> results = vectorStore.similaritySearch(request);

        return results.stream()
                .map(doc -> new SearchResult(
                        doc.getText(),
                        doc.getMetadata(),
                        doc.getScore()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 批量搜索
     */
    public Map<String, List<Document>> batchSearch(List<String> queries, int topK) {
        Map<String, List<Document>> results = new HashMap<>();

        for (String query : queries) {
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .build();
            results.put(query, vectorStore.similaritySearch(request));
        }

        return results;
    }

    /**
     * 搜索结果包装类
     */
    public static class SearchResult {
        private String content;
        private Map<String, Object> metadata;
        private Double score;

        public SearchResult(String content, Map<String, Object> metadata, Double score) {
            this.content = content;
            this.metadata = metadata;
            this.score = score;
        }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
    }

    // ==================== CRUD 方法 ====================

    public List<KnowledgeBase> getAllDocuments() {
        return knowledgeBaseMapper.findAll();
    }

    public KnowledgeBase getDocumentById(Long id) {
        return knowledgeBaseMapper.findById(id);
    }

    public void deleteDocument(Long id) {
        KnowledgeBase kb = knowledgeBaseMapper.findById(id);
        if (kb != null && kb.getDocumentPath() != null) {
            try {
                Files.deleteIfExists(Paths.get(kb.getDocumentPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        knowledgeBaseMapper.deleteById(id);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", knowledgeBaseMapper.count());
        stats.put("vectorizedDocuments", knowledgeBaseMapper.countByStatus("VECTORIZED"));
        stats.put("errorDocuments", knowledgeBaseMapper.countByStatus("ERROR"));
        return stats;
    }

    /**
     * 按标签搜索知识库
     */
    public List<KnowledgeBase> searchByTag(String tag) {
        return knowledgeBaseMapper.findByTag(tag);
    }
}