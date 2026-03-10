package com.tmccloud.aiapproval.service;

import com.tmccloud.aiapproval.entity.ExpenseClaim;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {
    
    @Autowired
    private ChatLanguageModel chatLanguageModel;
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    @Lazy
    private ExpenseClaimService expenseClaimService;
    
    /**
     * 检查报销单合规性（RAG场景）
     * @param expenseClaim 报销单信息
     * @return 合规性检查结果
     */
    public String checkCompliance(String expenseClaim) {
        // 检索相关的制度条款
        List<TextSegment> relevantClauses = documentService.searchRelevantClauses(expenseClaim);
        
        // 构建提示词，包含相关条款
        StringBuilder prompt = new StringBuilder();
        prompt.append("请根据公司《报销管理制度》的相关条款，检查以下报销单是否符合要求：\n");
        prompt.append("相关制度条款：\n");
        for (TextSegment clause : relevantClauses) {
            prompt.append("- ").append(clause.text()).append("\n");
        }
        prompt.append("\n报销单信息：\n").append(expenseClaim).append("\n");
        prompt.append("请回答：符合或不符合，并详细说明依据的制度条款。");
        
        return chatLanguageModel.generate(prompt.toString());
    }
    
    /**
     * 生成审批意见（Agent场景）
     * @param expenseClaim 报销单信息
     * @param complianceResult 合规性检查结果
     * @param userId 用户ID
     * @return 审批意见
     */
    public String generateApprovalOpinion(String expenseClaim, String complianceResult, Long userId) {
        // 检索用户历史报销记录
        List<ExpenseClaim> historyClaims = expenseClaimService.getExpenseClaimsByUserId(userId);
        
        // 构建提示词
        StringBuilder prompt = new StringBuilder();
        prompt.append("基于以下信息，生成一份个性化的审批意见：\n");
        prompt.append("报销单信息：\n").append(expenseClaim).append("\n");
        prompt.append("合规性检查结果：\n").append(complianceResult).append("\n");
        
        // 添加历史报销记录信息
        if (!historyClaims.isEmpty()) {
            prompt.append("用户历史报销记录：\n");
            for (int i = 0; i < Math.min(3, historyClaims.size()); i++) {
                ExpenseClaim historyClaim = historyClaims.get(i);
                prompt.append("- 日期：").append(historyClaim.getClaimDate())
                      .append("，类型：").append(historyClaim.getType())
                      .append("，金额：").append(historyClaim.getAmount())
                      .append("，状态：").append(historyClaim.getStatus()).append("\n");
            }
        }
        
        prompt.append("请考虑以下因素：\n");
        prompt.append("1. 报销金额是否合理\n");
        prompt.append("2. 报销类型是否符合公司政策\n");
        prompt.append("3. 报销理由是否充分\n");
        prompt.append("4. 基于公司政策的评估\n");
        prompt.append("5. 用户历史报销记录的模式和频率\n");
        prompt.append("请生成一份专业、客观的审批意见，包括对报销单的评估和建议。");
        
        return chatLanguageModel.generate(prompt.toString());
    }
    
    /**
     * 智能工作流推荐
     * @param expenseClaim 报销单信息
     * @return 推荐的审批人
     */
    public String recommendApprover(String expenseClaim) {
        // 分析报销单内容
        String prompt = "根据以下报销单信息，推荐合适的审批人：\n" +
                expenseClaim + "\n" +
                "规则：\n" +
                "1. 报销金额>5000，推荐财务总监\n" +
                "2. 差旅费，推荐部门经理\n" +
                "3. 业务招待费，推荐部门经理和财务\n" +
                "4. 特殊情况（如疫情出差），推荐特殊流程\n" +
                "请返回推荐的审批人或流程。";
        
        return chatLanguageModel.generate(prompt);
    }
}