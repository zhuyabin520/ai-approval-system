package com.tmccloud.aiapproval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("expense_item")
public class ExpenseItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long claimId;
    private String itemName;
    private BigDecimal itemAmount;
    private LocalDate itemDate;
    private String itemDescription;
    private String receiptUrl;      // 发票附件路径
}