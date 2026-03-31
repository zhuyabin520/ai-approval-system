package com.tmccloud.aiapproval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("expense_claim")
public class ExpenseClaim {
    @TableId(type = IdType.AUTO)
    private Long id; // 报销单号
    private Long userId;  // 员工ID
    private String department;  // 部门
    private BigDecimal amount; // 总金额
    private LocalDate claimDate; // 提交日期
    private String type; //费用类型（差旅费/招待费/办公用品等）
    private String description;   // 用途描述
    private String status;
    private String aiCheckResult;
    private String aiOpinion;
    private String processInstanceId;
    private String employeeName; // 员工姓名
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField(exist = false)
    private List<ExpenseItem> items;    // 费用明细
}