package com.tmccloud.aiapproval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_settings")
public class AISettings {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String modelName;
    private String apiKey;
    private String baseUrl;
    private BigDecimal temperature;
    private Integer maxTokens;
    private Integer timeout;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}