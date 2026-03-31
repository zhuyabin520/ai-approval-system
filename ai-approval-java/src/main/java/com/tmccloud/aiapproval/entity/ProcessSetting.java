package com.tmccloud.aiapproval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("APP_MODEL_LCSS_EFGH")
public class ProcessSetting {
    @TableId(type = IdType.AUTO)
    private Long leUnid;
    private String leUuid;
    private String leCmmUsageScenarios;
    private String leCreatorDeptUuid;
    private String leCreatorDeptName;
    private String leCreator;
    private String leCreatorName;
    private LocalDateTime leCreateTime;
    private String leLastUpdater;
    private String leLastUpdaterName;
    private LocalDateTime leLastUpdateTime;
    private String leStatus;
    private Long leOrder;
    private String leModelid;
    private String leText;
    private String leCmmFormUuid;
    private String leCmmEntityUuid;
    private String leCmmGroupUuid;
    
    // 业务自定义字段
    private String leProcessId;
    private String leProcessName;
    private String leStepId;
    private String leStepName;
    private Long leStepOrder;
    private String leResponsibleRole;
    private String leResponsibleUser;
    private String leCondition;
    private Long leDuration;
}