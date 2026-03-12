package com.tmccloud.aiapproval.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tmccloud.aiapproval.entity.ExpenseClaim;
import com.tmccloud.aiapproval.entity.ExpenseItem;
import com.tmccloud.aiapproval.entity.ApprovalRecord;
import com.tmccloud.aiapproval.mapper.ExpenseClaimMapper;
import com.tmccloud.aiapproval.mapper.ExpenseItemMapper;
import com.tmccloud.aiapproval.mapper.ApprovalRecordMapper;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ExpenseClaimService {
    
    @Autowired
    private ExpenseClaimMapper expenseClaimMapper;
    
    @Autowired
    private ExpenseItemMapper expenseItemMapper;
    
    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;
    
    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private AIService aiService;
    
    /**
     * 提交报销单
     * @param expenseClaim 报销单信息
     * @param items 报销明细项
     * @return 报销单ID
     */
    @Transactional
    public Long submitExpenseClaim(ExpenseClaim expenseClaim, List<ExpenseItem> items) {
        // 保存报销单
        expenseClaim.setStatus("SUBMITTED");
        expenseClaimMapper.insert(expenseClaim);
        
        // 保存报销明细项
        for (ExpenseItem item : items) {
            item.setClaimId(expenseClaim.getId());
            expenseItemMapper.insert(item);
        }
        
        // 启动审批流程
        Map<String, Object> variables = new HashMap<>();
        variables.put("claimId", expenseClaim.getId());
        variables.put("amount", expenseClaim.getAmount());
        variables.put("type", expenseClaim.getType());
        variables.put("description", expenseClaim.getDescription());
        variables.put("userId", expenseClaim.getUserId());
        String processInstanceId = runtimeService.startProcessInstanceByKey("expenseApprovalProcess", variables).getProcessInstanceId();
        
        // 更新报销单的流程实例ID
        expenseClaim.setProcessInstanceId(processInstanceId);
        expenseClaimMapper.updateById(expenseClaim);
        
        return expenseClaim.getId();
    }
    
    /**
     * 获取报销单详情
     * @param id 报销单ID
     * @return 报销单信息
     */
    public ExpenseClaim getExpenseClaimById(Long id) {
        return expenseClaimMapper.selectById(id);
    }
    
    /**
     * 获取报销明细项
     * @param claimId 报销单ID
     * @return 报销明细项列表
     */
    public List<ExpenseItem> getExpenseItemsByClaimId(Long claimId) {
        QueryWrapper<ExpenseItem> wrapper = new QueryWrapper<>();
        wrapper.eq("claim_id", claimId);
        return expenseItemMapper.selectList(wrapper);
    }
    
    /**
     * 审批报销单
     * @param taskId 任务ID
     * @param approved 是否批准
     * @param opinion 审批意见
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     */
    @Transactional
    public void approveExpenseClaim(String taskId, boolean approved, String opinion, Long approverId, String approverName) {
        // 完成任务
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        
        Map<String, Object> variables = new HashMap<>();
        String taskName = task.getName();
        
        if (taskName.equals("部门经理审批")) {
            variables.put("managerApprovalResult", approved ? "APPROVED" : "REJECTED");
        } else if (taskName.equals("财务审批")) {
            variables.put("financeApprovalResult", approved ? "APPROVED" : "REJECTED");
        }
        
        taskService.complete(taskId, variables);
        
        // 获取报销单ID
        ExpenseClaim expenseClaim = expenseClaimMapper.selectOne(
            new QueryWrapper<ExpenseClaim>().eq("process_instance_id", processInstanceId)
        );
        
        // 保存审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setClaimId(expenseClaim.getId());
        record.setApproverId(approverId);
        record.setApproverName(approverName);
        record.setApprovalStatus(approved ? "APPROVED" : "REJECTED");
        record.setApprovalOpinion(opinion);
        approvalRecordMapper.insert(record);
        
        // 更新报销单状态
        if (!approved) {
            expenseClaim.setStatus("REJECTED");
        } else if (taskName.equals("财务审批")) {
            expenseClaim.setStatus("APPROVED");
        } else {
            expenseClaim.setStatus("PENDING");
        }
        expenseClaimMapper.updateById(expenseClaim);
    }
    
    /**
     * 获取用户待审批任务
     * @param assignee 审批人
     * @return 待审批任务列表
     */
    public List<Task> getTasksByAssignee(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }
    
    /**
     * 获取报销单的审批记录
     * @param claimId 报销单ID
     * @return 审批记录列表
     */
    public List<ApprovalRecord> getApprovalRecordsByClaimId(Long claimId) {
        QueryWrapper<ApprovalRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("claim_id", claimId).orderByAsc("approval_date");
        return approvalRecordMapper.selectList(wrapper);
    }
    
    /**
     * 获取用户的历史报销记录
     * @param userId 用户ID
     * @return 报销单列表
     */
    public List<ExpenseClaim> getExpenseClaimsByUserId(Long userId) {
        QueryWrapper<ExpenseClaim> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        return expenseClaimMapper.selectList(wrapper);
    }
    
    /**
     * 获取所有报销单（用于前端展示）
     * @return 报销单列表
     */
    public List<ExpenseClaim> getAllExpenseClaims() {
        return expenseClaimMapper.selectList(null);
    }
    
    /**
     * 获取报销单列表（支持筛选和分页）
     * @param status 状态
     * @param userId 用户ID
     * @param claimId 报销单号
     * @param page 页码
     * @param size 每页大小
     * @return 报销单列表
     */
    public List<ExpenseClaim> getExpenseClaims(String status, Long userId, String claimId, Integer page, Integer size) {
        QueryWrapper<ExpenseClaim> wrapper = buildExpenseClaimQueryWrapper(status, userId, claimId);
        
        // 分页查询
        Page<ExpenseClaim> pageObj = new Page<>(page, size);
        Page<ExpenseClaim> result = expenseClaimMapper.selectPage(pageObj, wrapper);
        
        return result.getRecords();
    }
    
    /**
     * 获取报销单总数（支持筛选）
     * @param status 状态
     * @param userId 用户ID
     * @param claimId 报销单号
     * @return 总数
     */
    public long getExpenseClaimsCount(String status, Long userId, String claimId) {
        QueryWrapper<ExpenseClaim> wrapper = buildExpenseClaimQueryWrapper(status, userId, claimId);
        return expenseClaimMapper.selectCount(wrapper);
    }
    
    /**
     * 构建报销单查询条件
     * @param status 状态
     * @param userId 用户ID
     * @param claimId 报销单号
     * @return 查询条件
     */
    private QueryWrapper<ExpenseClaim> buildExpenseClaimQueryWrapper(String status, Long userId, String claimId) {
        QueryWrapper<ExpenseClaim> wrapper = new QueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }
        
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        
        if (claimId != null && !claimId.isEmpty()) {
            wrapper.like("id", claimId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc("created_at");
        
        return wrapper;
    }
    
    /**
     * 删除报销单
     * @param id 报销单ID
     * @return 删除是否成功
     */
    @Transactional
    public boolean deleteExpenseClaim(Long id) {
        // 删除关联的报销明细项
        QueryWrapper<ExpenseItem> itemWrapper = new QueryWrapper<>();
        itemWrapper.eq("claim_id", id);
        expenseItemMapper.delete(itemWrapper);
        
        // 删除关联的审批记录
        QueryWrapper<ApprovalRecord> recordWrapper = new QueryWrapper<>();
        recordWrapper.eq("claim_id", id);
        approvalRecordMapper.delete(recordWrapper);
        
        // 删除报销单
        int result = expenseClaimMapper.deleteById(id);
        
        return result > 0;
    }
}