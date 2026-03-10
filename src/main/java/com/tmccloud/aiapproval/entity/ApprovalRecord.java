package com.tmccloud.aiapproval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long claimId;
    private Long approverId;
    private String approverName;
    private LocalDateTime approvalDate;
    private String approvalStatus;
    private String approvalOpinion;
}