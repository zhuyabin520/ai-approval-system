package com.tmccloud.aiapproval.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("APP_MODEL_JSQX_ABCD")
public class RolePermission {
    @TableId(type = IdType.AUTO)
    private Long jaUnid;
    private String jaUuid;
    private String jaCmmUsageScenarios;
    private String jaCreatorDeptUuid;
    private String jaCreatorDeptName;
    private String jaCreator;
    private String jaCreatorName;
    private LocalDateTime jaCreateTime;
    private String jaLastUpdater;
    private String jaLastUpdaterName;
    private LocalDateTime jaLastUpdateTime;
    private String jaStatus;
    private Long jaOrder;
    private String jaModelid;
    private String jaText;
    private String jaCmmFormUuid;
    private String jaCmmEntityUuid;
    private String jaCmmGroupUuid;
    
    // 业务自定义字段
    private String jaRoleId;
    private String jaRoleName;
    private String jaPermissionId;
    private String jaPermissionName;
    private String jaPermissionDesc;
    private String jaModule;
    private String jaAction;
}