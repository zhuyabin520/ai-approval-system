package com.tmccloud.aiapproval;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tmccloud.aiapproval.mapper")
public class AiApprovalSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AiApprovalSystemApplication.class, args);
    }
}