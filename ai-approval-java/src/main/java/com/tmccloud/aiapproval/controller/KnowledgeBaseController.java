package com.tmccloud.aiapproval.controller;

import com.tmccloud.aiapproval.entity.KnowledgeBase;
import com.tmccloud.aiapproval.service.KnowledgeBaseService;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge-base")
public class KnowledgeBaseController {
    
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    
    @PostMapping("/upload")
    public ResponseEntity<KnowledgeBase> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            @RequestParam("userId") Long userId,
            @RequestParam("tags") String tags) {
        try {
            KnowledgeBase knowledgeBase = knowledgeBaseService.uploadDocument(file, description, userId, tags);
            return new ResponseEntity<>(knowledgeBase, HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<KnowledgeBase>> getAllDocuments() {
        List<KnowledgeBase> documents = knowledgeBaseService.getAllDocuments();
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeBase> getDocumentById(@PathVariable Long id) {
        KnowledgeBase document = knowledgeBaseService.getDocumentById(id);
        if (document != null) {
            return new ResponseEntity<>(document, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        knowledgeBaseService.deleteDocument(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/search-by-tag")
    public ResponseEntity<List<KnowledgeBase>> searchByTag(@RequestParam("tag") String tag) {
        List<KnowledgeBase> documents = knowledgeBaseService.searchByTag(tag);
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }
    @GetMapping("/search")
    public List<Document> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        return knowledgeBaseService.searchSimilarWithTopK(query, topK);
    }

    @GetMapping("/search/threshold")
    public List<Document> searchWithThreshold(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "0.7") double threshold) {
        return knowledgeBaseService.searchSimilarWithThreshold(query, topK, threshold);
    }

    @GetMapping("/search/knowledge-base")
    public List<Document> searchByKnowledgeBase(
            @RequestParam String query,
            @RequestParam Long knowledgeBaseId,
            @RequestParam(defaultValue = "5") int topK) {
        return knowledgeBaseService.searchByKnowledgeBase(query, knowledgeBaseId, topK);
    }

    @GetMapping("/search/filter")
    public List<Document> searchWithFilter(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(required = false) Long knowledgeBaseId,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Double minSimilarity) {
        return knowledgeBaseService.searchWithMultipleFilters(query, topK, knowledgeBaseId, tags, minSimilarity);
    }

    @GetMapping("/search/scores")
    public List<KnowledgeBaseService.SearchResult> searchWithScores(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {
        return knowledgeBaseService.searchWithScores(query, topK);
    }

}