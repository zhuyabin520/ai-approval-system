package com.tmccloud.aiapproval.controller;

import com.tmccloud.aiapproval.service.TextToSQLService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/text-to-sql")
public class TextToSQLController {
    
    @Autowired
    private TextToSQLService textToSQLService;
    
    @PostMapping("/query")
    public ResponseEntity<Map<String, Object>> query(@RequestBody String naturalLanguageQuery) {
        try {
            Map<String, Object> result = textToSQLService.generateAndExecuteSQL(naturalLanguageQuery);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "处理查询时出错，请稍后再试。"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/explain")
    public ResponseEntity<String> explain(@RequestBody String sql) {
        try {
            String explanation = textToSQLService.explainSQL(sql);
            return new ResponseEntity<>(explanation, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("处理SQL解释时出错，请稍后再试。", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}