package com.tmccloud.aiapproval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.autoconfigure.vectorstore.milvus.MilvusVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {MilvusVectorStoreAutoConfiguration.class})
@MapperScan("com.tmccloud.aiapproval.mapper")
public class AiApprovalSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApprovalSystemApplication.class, args);
    }
}