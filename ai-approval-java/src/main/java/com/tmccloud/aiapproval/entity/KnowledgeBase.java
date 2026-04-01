package com.tmccloud.aiapproval.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeBase {
    private Long id;
    private String name;
    private String description;
    private String documentPath;
    private String documentType;
    private Long fileSize;
    private String content;
    private LocalDateTime uploadTime;
    private LocalDateTime vectorizationTime;
    private String status;
    private Long userId;
    private String tags;
    private String errorMessage;
    private Integer chunkCount;
}