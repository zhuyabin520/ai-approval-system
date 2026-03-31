package com.tmccloud.aiapproval.controller;

import com.tmccloud.aiapproval.entity.ExpenseCheckResult;
import com.tmccloud.aiapproval.entity.ExpenseClaim;
import com.tmccloud.aiapproval.service.ExpenseApprovalAssistant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * 审批助手 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ExpenseApprovalAssistant approvalAssistant;

    /**
     * 审核报销单
     */
    @PostMapping("/check")
    public ExpenseCheckResult checkExpense(@RequestBody ExpenseClaim report) {
        log.info("收到报销审核请求：{}", report.getId());

        // 格式化费用明细
        String expenseItems = report.getItems().stream()
                .map(item -> String.format("- %s: %.2f元 (%s)",
                        item.getItemName(), item.getItemAmount(), item.getItemDescription()))
                .collect(Collectors.joining("\n"));

        // 调用 AI 审批助手
        ExpenseCheckResult result = approvalAssistant.checkExpense(
                report.getEmployeeName(),
                report.getDepartment(),
                report.getType(),
                report.getAmount(),
                report.getDescription(),
                report.getClaimDate().toString(),
                expenseItems
        );

        log.info("审核完成：{}", result.getDecision());
        return result;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public String health() {
        return "Approval Assistant is running";
    }
}