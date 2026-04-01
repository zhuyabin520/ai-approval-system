package com.tmccloud.aiapproval.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.tmccloud.aiapproval.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RAGController {
    
    @Autowired
    private RAGService ragService;

    @PostMapping("/query")
    public ResponseEntity<String> query(@RequestBody String query) {
        try {
            String answer = ragService.generateAnswer(query);
            return new ResponseEntity<>(answer, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("处理查询时出错，请稍后再试。", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/query-with-context")
    public ResponseEntity<String> queryWithContext(@RequestParam("query") String query, @RequestParam("context") String context) {
        try {
            String answer = ragService.generateAnswerWithContext(query, context);
            return new ResponseEntity<>(answer, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("处理查询时出错，请稍后再试。", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}