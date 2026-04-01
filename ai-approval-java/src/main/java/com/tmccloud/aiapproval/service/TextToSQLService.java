package com.tmccloud.aiapproval.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TextToSQLService {
    
    @Autowired
    private ChatLanguageModel chatLanguageModel;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private static final String DATABASE_SCHEMA = "" +
            "数据库表结构：\n" +
            "1. expense_claim (报销申请)\n" +
            "   - id: BIGINT, 主键\n" +
            "   - user_id: BIGINT, 用户ID\n" +
            "   - total_amount: DECIMAL(10,2), 总金额\n" +
            "   - submit_time: DATETIME, 提交时间\n" +
            "   - status: VARCHAR(20), 状态（PENDING, APPROVED, REJECTED）\n" +
            "   - description: VARCHAR(255), 描述\n" +
            "\n" +
            "2. expense_item (报销项)\n" +
            "   - id: BIGINT, 主键\n" +
            "   - claim_id: BIGINT, 关联报销申请ID\n" +
            "   - amount: DECIMAL(10,2), 金额\n" +
            "   - category: VARCHAR(50), 类别\n" +
            "   - date: DATE, 日期\n" +
            "   - description: VARCHAR(255), 描述\n" +
            "   - receipt_url: VARCHAR(255), 收据URL\n" +
            "\n" +
            "3. user (用户)\n" +
            "   - id: BIGINT, 主键\n" +
            "   - username: VARCHAR(50), 用户名\n" +
            "   - name: VARCHAR(50), 姓名\n" +
            "   - department: VARCHAR(50), 部门\n" +
            "   - position: VARCHAR(50), 职位\n" +
            "\n" +
            "4. approval_record (审批记录)\n" +
            "   - id: BIGINT, 主键\n" +
            "   - claim_id: BIGINT, 关联报销申请ID\n" +
            "   - approver_id: BIGINT, 审批人ID\n" +
            "   - approval_time: DATETIME, 审批时间\n" +
            "   - status: VARCHAR(20), 审批状态（APPROVED, REJECTED）\n" +
            "   - comment: VARCHAR(255), 审批意见\n";
    
    public Map<String, Object> generateAndExecuteSQL(String naturalLanguageQuery) {
        // 1. 生成SQL语句
        String sql = generateSQL(naturalLanguageQuery);
        
        // 2. 执行SQL语句
        List<Map<String, Object>> results = executeSQL(sql);
        
        // 3. 构建响应
        return Map.of(
                "sql", sql,
                "results", results,
                "count", results.size()
        );
    }
    
    private String generateSQL(String naturalLanguageQuery) {
        // 构建提示词
        String prompt = "你是一个SQL专家，根据以下数据库表结构，将自然语言查询转换为SQL语句。\n\n" +
                DATABASE_SCHEMA +
                "\n自然语言查询：" + naturalLanguageQuery + "\n\n" +
                "请只返回SQL语句，不要包含任何其他内容。确保SQL语句语法正确，并且能够准确回答查询。";
        
        // 调用大模型生成SQL
        String sql = chatLanguageModel.generate(prompt);
        
        // 清理SQL语句，移除可能的标记或多余内容
        sql = sql.trim();
        if (sql.startsWith("```sql")) {
            sql = sql.substring(6);
        }
        if (sql.endsWith("```")) {
            sql = sql.substring(0, sql.length() - 3);
        }
        
        return sql;
    }
    
    private List<Map<String, Object>> executeSQL(String sql) {
        try {
            // 执行SQL查询
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            e.printStackTrace();
            // 返回错误信息
            return List.of(Map.of("error", e.getMessage()));
        }
    }
    
    public String explainSQL(String sql) {
        // 构建提示词
        String prompt = "你是一个SQL专家，请解释以下SQL语句的含义：\n\n" + sql + "\n\n" +
                "请用通俗易懂的语言解释该SQL语句的功能、执行逻辑和可能的结果。";
        
        // 调用大模型生成解释
        return chatLanguageModel.generate(prompt);
    }
}