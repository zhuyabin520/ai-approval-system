package com.tmccloud.aiapproval.controller;

import com.tmccloud.aiapproval.entity.ExpenseClaim;
import com.tmccloud.aiapproval.entity.ExpenseItem;
import com.tmccloud.aiapproval.entity.ApprovalRecord;
import com.tmccloud.aiapproval.service.ExpenseClaimService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expense")
public class ExpenseClaimController {
    
    @Autowired
    private ExpenseClaimService expenseClaimService;
    
    /**
     * 获取报销单列表
     * @param status 状态（可选）
     * @param userId 用户 ID（可选）
     * @return 报销单列表
     */
    @GetMapping("/list")
    public List<ExpenseClaim> getExpenseClaims(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        if (userId != null) {
            return expenseClaimService.getExpenseClaimsByUserId(userId);
        }
        return expenseClaimService.getAllExpenseClaims();
    }
    
    /**
     * 提交报销单
     * @param request 请求数据，包含报销单信息和明细项
     * @return 报销单 ID
     */
    @PostMapping("/submit")
    public Long submitExpenseClaim(@RequestBody Map<String, Object> request) {
        ExpenseClaim expenseClaim = new ExpenseClaim();
        // 将 String 类型的 userId 转换为 Long
        Object userIdObj = request.get("userId");
        Long userId = null;
        if (userIdObj instanceof String) {
            userId = Long.valueOf((String) userIdObj);
        } else if (userIdObj instanceof Number) {
            userId = ((Number) userIdObj).longValue();
        }
        expenseClaim.setUserId(userId);
        
        expenseClaim.setAmount(new java.math.BigDecimal(request.get("amount").toString()));
        // 处理日期格式
        String claimDateStr = request.get("claimDate").toString();
        expenseClaim.setClaimDate(java.time.LocalDate.parse(claimDateStr.split("T")[0]));
        expenseClaim.setType((String) request.get("type"));
        expenseClaim.setDescription((String) request.get("description"));
        
        // 处理明细项
        List<ExpenseItem> items = null;
        Object itemsObj = request.get("items");
        if (itemsObj instanceof List) {
            items = (List<ExpenseItem>) itemsObj;
        }
        
        return expenseClaimService.submitExpenseClaim(expenseClaim, items);
    }
    
    /**
     * 获取报销单详情
     * @param id 报销单ID
     * @return 报销单信息
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getExpenseClaimDetail(@PathVariable Long id) {
        ExpenseClaim claim = expenseClaimService.getExpenseClaimById(id);
        List<ExpenseItem> items = expenseClaimService.getExpenseItemsByClaimId(id);
        List<ApprovalRecord> records = expenseClaimService.getApprovalRecordsByClaimId(id);
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("claim", claim);
        result.put("items", items);
        result.put("approvalRecords", records);
        return result;
    }
    
    /**
     * 审批报销单
     * @param taskId 任务ID
     * @param request 审批信息
     */
    @PostMapping("/approve/{taskId}")
    public void approveExpenseClaim(@PathVariable String taskId, @RequestBody Map<String, Object> request) {
        boolean approved = (boolean) request.get("approved");
        String opinion = (String) request.get("opinion");
        // 将 String 类型的 approverId 转换为 Long
        Object approverIdObj = request.get("approverId");
        Long approverId = null;
        if (approverIdObj instanceof String) {
            approverId = Long.valueOf((String) approverIdObj);
        } else if (approverIdObj instanceof Number) {
            approverId = ((Number) approverIdObj).longValue();
        }
        String approverName = (String) request.get("approverName");
        
        expenseClaimService.approveExpenseClaim(taskId, approved, opinion, approverId, approverName);
    }
    
    /**
     * 获取用户待审批任务
     * @param assignee 审批人
     * @return 待审批任务列表
     */
    @GetMapping("/tasks/{assignee}")
    public List<Task> getTasksByAssignee(@PathVariable String assignee) {
        return expenseClaimService.getTasksByAssignee(assignee);
    }
    
    /**
     * 获取报销单的审批记录
     * @param claimId 报销单ID
     * @return 审批记录列表
     */
    @GetMapping("/approval-records/{claimId}")
    public List<ApprovalRecord> getApprovalRecords(@PathVariable Long claimId) {
        return expenseClaimService.getApprovalRecordsByClaimId(claimId);
    }
}