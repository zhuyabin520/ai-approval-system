package com.tmccloud.aiapproval.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RAGService {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private EmbeddingModel embeddingModel;

    private static final int TOP_K = 5;
    private static final double SIMILARITY_THRESHOLD = 0.7;

    public String generateAnswer(String query) {
        List<Document> relevantDocs = retrieveRelevantDocuments(query);

        if (relevantDocs.isEmpty()) {
            return "抱歉，知识库中没有找到与您的问题相关的信息。";
        }

        String context = buildContext(relevantDocs);

        return chatClient.prompt()
                .user(buildPrompt(query, context))
                .call()
                .content();
    }

    private List<Document> retrieveRelevantDocuments(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }

    private String buildContext(List<Document> documents) {
        return documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
    }

    private String buildPrompt(String query, String context) {
        return String.format(
                """
                你是一个专业的报销领域智能助手，根据以下提供的知识库信息和用户问题，给出准确、详细的回答。
                
                知识库信息：
                %s
                
                用户问题：
                %s
                
                请根据知识库信息回答用户问题，确保回答准确、专业。如果知识库中没有相关信息，请明确说明。
                """,
                context,
                query
        );
    }

    public String generateAnswerWithContext(String query, String context) {
        String prompt = String.format(
                """
                你是一个专业的报销领域智能助手，根据以下上下文和用户问题，给出准确、详细的回答。
                
                上下文：
                %s
                
                用户问题：
                %s
                
                请根据上下文回答用户问题，确保回答准确、专业。
                """,
                context,
                query
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    public String chatWithRAG(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        if (documents.isEmpty()) {
            return chatClient.prompt()
                    .user(query)
                    .call()
                    .content();
        }

        String context = buildContext(documents);
        return chatClient.prompt()
                .user(buildPrompt(query, context))
                .call()
                .content();
    }
}
