package com.tmccloud.aiapproval.service;

import com.tmccloud.aiapproval.entity.ExpenseCheckResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.math.BigDecimal;

/**
 * AI 审批助手接口
 * 使用 LangChain4j 的 @AiService 注解，自动实现RAG和结构化输出[citation:7][citation:9]
 */
public interface ExpenseApprovalAssistant {

    @SystemMessage("""
        你是一个专业的财务审批助手，精通企业报销制度和财务规范。
        
        你的核心职责是：
        1. 严格依据公司制度文档审核报销单的合规性
        2. 如果信息不全，明确指出需要补充什么
        3. 对于违规报销，必须引用具体的制度条款编号
        4. 基于历史审批模式，建议合适的审批人
        
        请始终保持客观、严谨，输出必须符合指定的JSON格式。
        """)

    @UserMessage("""
        请审核以下报销单：
        
        【报销单信息】
        员工：{{employeeName}}
        部门：{{department}}
        费用类型：{{expenseType}}
        总金额：{{totalAmount}}元
        用途描述：{{description}}
        提交日期：{{submitDate}}
        
        【费用明细】
        {{expenseItems}}
        
        请基于公司制度文档，给出审核结果。
        必须严格按照 JSON Schema 格式输出，包含 decision/reason/confidence 等字段。
        """)
    ExpenseCheckResult checkExpense(
            @V("employeeName") String employeeName,
            @V("department") String department,
            @V("expenseType") String expenseType,
            @V("totalAmount") BigDecimal totalAmount,
            @V("description") String description,
            @V("submitDate") String submitDate,
            @V("expenseItems") String expenseItems
    );
}