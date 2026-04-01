package com.tmccloud.aiapproval.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档切片服务（适配 Spring AI 1.0.0-M6）
 */
@Service
public class DocumentChunkingService {

    private static final int DEFAULT_CHUNK_SIZE = 800;
    private static final int DEFAULT_CHUNK_OVERLAP = 200;
    private static final int DEFAULT_MIN_CHUNK_SIZE = 50;
    private static final int DEFAULT_MAX_CHUNK_SIZE = 2000;

    /**
     * 使用 TokenTextSplitter 进行切片（推荐）
     */
    public List<Document> chunkDocument(List<Document> documents) {
        return chunkDocument(documents, DEFAULT_CHUNK_SIZE, DEFAULT_CHUNK_OVERLAP);
    }

    /**
     * 自定义参数的 Token 切片
     */
    public List<Document> chunkDocument(List<Document> documents, int chunkSize, int overlap) {
        // 创建 TokenTextSplitter
        // 参数：chunkSize, overlap, minChunkSize, maxChunkSize, keepSeparator
        TextSplitter splitter = new TokenTextSplitter(
                chunkSize,      // 每块最大 token 数
                overlap,        // 重叠 token 数
                DEFAULT_MIN_CHUNK_SIZE,
                DEFAULT_MAX_CHUNK_SIZE,
                true            // 保留分隔符
        );

        // 执行切片
        return splitter.apply(documents);
    }

    /**
     * 按字符数切片（简单实现）
     */
    public List<Document> chunkByCharacters(Document document, int chunkSize, int overlap) {
        String text = document.getText();  // 使用 getText() 而不是 getContent()
        Map<String, Object> originalMetadata = document.getMetadata();

        List<Document> chunks = new ArrayList<>();
        int start = 0;
        int length = text.length();

        while (start < length) {
            int end = Math.min(start + chunkSize, length);
            String chunkText = text.substring(start, end);

            // 创建新的 Document，复制原 metadata
            Document chunk = new Document(chunkText, new HashMap<>(originalMetadata));
            chunk.getMetadata().put("chunkIndex", chunks.size());
            chunk.getMetadata().put("chunkStart", start);
            chunk.getMetadata().put("chunkEnd", end);

            chunks.add(chunk);
            start += chunkSize - overlap;
        }

        return chunks;
    }

    /**
     * 按段落切片（保持语义完整性）
     */
    public List<Document> chunkByParagraph(Document document) {
        String text = document.getText();
        Map<String, Object> originalMetadata = document.getMetadata();

        // 按空行分割段落
        String[] paragraphs = text.split("\\n\\s*\\n");

        List<Document> chunks = new ArrayList<>();
        for (int i = 0; i < paragraphs.length; i++) {
            String paragraph = paragraphs[i].trim();
            if (!paragraph.isEmpty()) {
                Document chunk = new Document(paragraph, new HashMap<>(originalMetadata));
                chunk.getMetadata().put("chunkIndex", i);
                chunk.getMetadata().put("chunkType", "paragraph");
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    /**
     * 按句子切片（使用简单规则）
     */
    public List<Document> chunkBySentence(Document document) {
        String text = document.getText();
        Map<String, Object> originalMetadata = document.getMetadata();

        // 按句子分割（中文和英文）
        String[] sentences = text.split("(?<=[。！？.!?])\\s*");

        List<Document> chunks = new ArrayList<>();
        for (int i = 0; i < sentences.length; i++) {
            String sentence = sentences[i].trim();
            if (!sentence.isEmpty()) {
                Document chunk = new Document(sentence, new HashMap<>(originalMetadata));
                chunk.getMetadata().put("chunkIndex", i);
                chunk.getMetadata().put("chunkType", "sentence");
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    /**
     * 混合切片：优先按段落，超长段落再按句子
     */
    public List<Document> chunkByMixed(Document document, int maxTokensPerChunk) {
        String text = document.getText();
        Map<String, Object> originalMetadata = document.getMetadata();

        // 先按段落分割
        String[] paragraphs = text.split("\\n\\s*\\n");

        List<Document> chunks = new ArrayList<>();

        for (String paragraph : paragraphs) {
            int estimatedTokens = estimateTokens(paragraph);

            if (estimatedTokens <= maxTokensPerChunk) {
                // 段落长度合适，直接作为一个块
                Document chunk = new Document(paragraph, new HashMap<>(originalMetadata));
                chunk.getMetadata().put("chunkType", "paragraph");
                chunks.add(chunk);
            } else {
                // 段落太长，按句子分割
                String[] sentences = paragraph.split("(?<=[。！？.!?])\\s*");
                StringBuilder currentChunk = new StringBuilder();
                int currentTokens = 0;

                for (String sentence : sentences) {
                    int sentenceTokens = estimateTokens(sentence);

                    if (currentTokens + sentenceTokens > maxTokensPerChunk && currentChunk.length() > 0) {
                        Document chunk = new Document(currentChunk.toString(), new HashMap<>(originalMetadata));
                        chunk.getMetadata().put("chunkType", "sentence_group");
                        chunks.add(chunk);
                        currentChunk = new StringBuilder();
                        currentTokens = 0;
                    }

                    if (currentChunk.length() > 0) {
                        currentChunk.append(" ");
                    }
                    currentChunk.append(sentence);
                    currentTokens += sentenceTokens;
                }

                if (currentChunk.length() > 0) {
                    Document chunk = new Document(currentChunk.toString(), new HashMap<>(originalMetadata));
                    chunk.getMetadata().put("chunkType", "sentence_group");
                    chunks.add(chunk);
                }
            }
        }

        // 添加索引元数据
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).getMetadata().put("chunkIndex", i);
            chunks.get(i).getMetadata().put("totalChunks", chunks.size());
        }

        return chunks;
    }

    /**
     * 为文档添加元数据增强（关键词、摘要等）
     * 可选功能，需要 ChatModel 支持
     */
    public List<Document> enrichWithMetadata(List<Document> documents) {
        // 这里可以添加：
        // 1. 为每个 chunk 生成关键词
        // 2. 生成摘要
        // 3. 添加时间戳等

        for (Document doc : documents) {
            doc.getMetadata().put("enrichedAt", System.currentTimeMillis());
            doc.getMetadata().put("textLength", doc.getText().length());
            doc.getMetadata().put("estimatedTokens", estimateTokens(doc.getText()));
        }

        return documents;
    }

    /**
     * 估算 token 数量（简单估算）
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        // 简单估算：中英文混合场景下，约 1.5-2 字符 = 1 token
        // 这里使用保守估算
        return (int) Math.ceil(text.length() / 2.5);
    }

    /**
     * 获取切片统计信息
     */
    public Map<String, Object> getChunkStatistics(List<Document> chunks) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChunks", chunks.size());

        List<Integer> lengths = chunks.stream()
                .map(doc -> doc.getText().length())
                .collect(Collectors.toList());

        stats.put("avgLength", lengths.stream().mapToInt(Integer::intValue).average().orElse(0));
        stats.put("minLength", lengths.stream().mapToInt(Integer::intValue).min().orElse(0));
        stats.put("maxLength", lengths.stream().mapToInt(Integer::intValue).max().orElse(0));

        return stats;
    }
}