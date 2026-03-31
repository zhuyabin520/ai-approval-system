package com.tmccloud.aiapproval.listener;


import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

@Component
public class FinanceApprovalTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        // 财务审批任务创建时的逻辑
        System.out.println("财务审批任务创建" + delegateTask.getName());
        // 可以在这里设置任务的相关属性
    }
}