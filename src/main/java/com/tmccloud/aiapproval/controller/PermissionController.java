package com.tmccloud.aiapproval.controller;

import com.tmccloud.aiapproval.entity.ProcessSetting;
import com.tmccloud.aiapproval.entity.RolePermission;
import com.tmccloud.aiapproval.service.ProcessSettingService;
import com.tmccloud.aiapproval.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permission")
public class PermissionController {
    
    @Autowired
    private RolePermissionService rolePermissionService;
    
    @Autowired
    private ProcessSettingService processSettingService;
    
    // 角色权限相关API
    
    /**
     * 获取所有角色权限
     */
    @GetMapping("/role-permissions")
    public List<RolePermission> getRolePermissions() {
        return rolePermissionService.getAllRolePermissions();
    }
    
    /**
     * 根据角色ID获取权限
     */
    @GetMapping("/role-permissions/{roleId}")
    public List<RolePermission> getPermissionsByRole(@PathVariable String roleId) {
        return rolePermissionService.getPermissionsByRoleId(roleId);
    }
    
    /**
     * 保存角色权限
     */
    @PostMapping("/role-permissions")
    public void saveRolePermission(@RequestBody RolePermission rolePermission) {
        rolePermissionService.saveRolePermission(rolePermission);
    }
    
    /**
     * 删除角色权限
     */
    @DeleteMapping("/role-permissions/{id}")
    public void deleteRolePermission(@PathVariable Long id) {
        rolePermissionService.deleteRolePermission(id);
    }
    
    /**
     * 根据角色和权限删除
     */
    @DeleteMapping("/role-permissions/{roleId}/{permissionId}")
    public void deleteByRoleAndPermission(@PathVariable String roleId, @PathVariable String permissionId) {
        rolePermissionService.deleteByRoleAndPermission(roleId, permissionId);
    }
    
    // 流程设置相关API
    
    /**
     * 获取所有流程设置
     */
    @GetMapping("/process-settings")
    public List<ProcessSetting> getProcessSettings() {
        return processSettingService.getAllProcessSettings();
    }
    
    /**
     * 根据流程ID获取步骤
     */
    @GetMapping("/process-settings/{processId}")
    public List<ProcessSetting> getStepsByProcess(@PathVariable String processId) {
        return processSettingService.getStepsByProcessId(processId);
    }
    
    /**
     * 保存流程设置
     */
    @PostMapping("/process-settings")
    public void saveProcessSetting(@RequestBody ProcessSetting processSetting) {
        processSettingService.saveProcessSetting(processSetting);
    }
    
    /**
     * 删除流程设置
     */
    @DeleteMapping("/process-settings/{id}")
    public void deleteProcessSetting(@PathVariable Long id) {
        processSettingService.deleteProcessSetting(id);
    }
    
    /**
     * 根据流程和步骤删除
     */
    @DeleteMapping("/process-settings/{processId}/{stepId}")
    public void deleteByProcessAndStep(@PathVariable String processId, @PathVariable String stepId) {
        processSettingService.deleteByProcessAndStep(processId, stepId);
    }
}