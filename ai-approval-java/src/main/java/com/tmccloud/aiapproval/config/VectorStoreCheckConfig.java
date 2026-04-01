package com.tmccloud.aiapproval.config;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class VectorStoreCheckConfig {

    private static final Logger logger = LoggerFactory.getLogger(VectorStoreCheckConfig.class);

    @Bean
    public CommandLineRunner checkVectorStore(VectorStore vectorStore) {
        return args -> {
            logger.info("=== VectorStore 检查 ===");
            logger.info("VectorStore Bean: {}", vectorStore);
            logger.info("VectorStore 类型: {}", vectorStore.getClass().getName());

            if (vectorStore == null) {
                logger.error("VectorStore Bean 为空！");
            } else {
                logger.info("VectorStore Bean 注入成功");
            }

            logger.info("=== VectorStore 检查完成 ===");
        };
    }
}