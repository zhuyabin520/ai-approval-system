package com.tmccloud.aiapproval.config;


import dev.langchain4j.model.chat.request.json.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class JsonSchemaConfig {

    @Bean
    public JsonSchema expenseCheckJsonSchema() {
        // 1. 构建枚举类型（decision）
        JsonEnumSchema decisionEnum = JsonEnumSchema.builder()
                .description("审批决定：通过、拒绝或需要更多信息")
                .enumValues(List.of("approved", "rejected", "need_more_info"))
                .build();

        // 2. 构建字符串类型（reason）
        JsonStringSchema reasonSchema = JsonStringSchema.builder()
                .description("判断理由，需引用具体制度条款")
                .build();

        // 3. 构建数组类型（violatedRules），数组项是字符串
        JsonArraySchema violatedRulesArray = JsonArraySchema.builder()
                .description("违反的制度条款列表（仅当拒绝时填写）")
                .items(JsonStringSchema.builder()
                        .description("制度条款编号，如：POLICY-101")
                        .build())
                .build();

        // 4. 构建字符串类型（suggestedApprover）
        JsonStringSchema suggestedApproverSchema = JsonStringSchema.builder()
                .description("建议的下一级审批人（可选）")
                .build();

        // 5. 构建数字类型（confidence）
        JsonNumberSchema confidenceSchema = JsonNumberSchema.builder()
                .description("判断置信度，0-1之间的数值")
                .build();

        // 6. 将所有属性放入Map
        Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
        properties.put("decision", decisionEnum);
        properties.put("reason", reasonSchema);
        properties.put("violatedRules", violatedRulesArray);
        properties.put("suggestedApprover", suggestedApproverSchema);
        properties.put("confidence", confidenceSchema);

        // 7. 构建根对象（使用properties方法设置整个Map）
        JsonObjectSchema rootSchema = JsonObjectSchema.builder()
                .description("报销审核结果")
                .properties(properties)                       // 关键：这里是properties，不是addProperty
                .required(List.of("decision", "reason", "confidence"))
                .build();

        // 8. 返回最终的JsonSchema
        return JsonSchema.builder()
                .name("ExpenseCheckResult")
                .rootElement(rootSchema)
                .build();
    }
}