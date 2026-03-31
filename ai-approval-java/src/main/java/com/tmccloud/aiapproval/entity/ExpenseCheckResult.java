package com.tmccloud.aiapproval.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 报销审核结果DTO - 与JSON Schema严格对应
 * 用于强制AI输出的结构化数据格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCheckResult {

    @JsonProperty("decision")
    private Decision decision;  // 审批决定

    @JsonProperty("reason")
    private String reason;      // 判断理由

    @JsonProperty("violatedRules")
    private List<String> violatedRules;  // 违反的制度条款（如有）

    @JsonProperty("suggestedApprover")
    private String suggestedApprover;    // 建议的审批人（可选）

    @JsonProperty("confidence")
    private Double confidence;            // 判断置信度 0-1

    /**
     * 审批决定枚举 - 对应JSON Schema中的enum
     */
    public enum Decision {
        @JsonProperty("approved") APPROVED,
        @JsonProperty("rejected") REJECTED,
        @JsonProperty("need_more_info") NEED_MORE_INFO
    }
}