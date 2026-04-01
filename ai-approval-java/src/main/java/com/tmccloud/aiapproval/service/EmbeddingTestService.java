package com.tmccloud.aiapproval.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EmbeddingTestService {


    @Autowired
    private EmbeddingModel embeddingModel;

// ... existing code ...

    @PostConstruct
    public void testEmbedding() {
        try {
            log.info("=== 测试 EmbeddingModel ===");
            log.info("EmbeddingModel 实现类：{}", embeddingModel.getClass().getName());

            String testText = "这是一个测试文本";
            log.info("测试文本：{}", testText);

            float[] embedding = embeddingModel.embed(testText);
            log.info("向量生成成功，维度：{}", embedding.length);
            int length = embedding.length;
            // 将 float[] 转换为 List<Float> 并取前 10 个值
            float v = embedding[0];
            log.info("前 10 个值：{}", v);

            log.info("=== EmbeddingModel 测试成功 ===");
        } catch (Exception e) {
            log.error("=== EmbeddingModel 测试失败 ===");
            log.error("错误：{}", e.getMessage(), e);
        }
    }

// ... existing code ...

}