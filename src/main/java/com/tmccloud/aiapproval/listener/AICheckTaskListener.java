package com.tmccloud.aiapproval.listener;

import com.tmccloud.aiapproval.service.AIService;
import jakarta.annotation.Resource;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class AICheckTaskListener implements TaskListener, ApplicationContextAware {

    private static ApplicationContext applicationContext;
    private AIService aiService;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }



    
    @Override
    public void notify(DelegateTask delegateTask) {
        // 获取报销单信息
        Long claimId = (Long) delegateTask.getVariable("claimId");
        String expenseClaim = "报销单 ID: " + claimId + "\n" +
                "金额: " + delegateTask.getVariable("amount") + "\n" +
                "类型: " + delegateTask.getVariable("type") + "\n" +
                "描述: " + delegateTask.getVariable("description");
        
        // 进行 AI 合规性检查
        if (aiService == null) {
            aiService = applicationContext.getBean(AIService.class);
        }

        String complianceResult = aiService.checkCompliance(expenseClaim);
        Long userId = (Long) delegateTask.getVariable("userId");
        // 生成审批意见
        String aiOpinion = aiService.generateApprovalOpinion(expenseClaim, complianceResult, userId);
        
        // 设置检查结果和意见
        if (complianceResult.contains("不符合")) {
            delegateTask.setVariable("aiCheckResult", "REJECTED");
        } else {
            delegateTask.setVariable("aiCheckResult", "APPROVED");
        }
        delegateTask.setVariable("aiOpinion", aiOpinion);
    }
}