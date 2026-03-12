package com.tmccloud.aiapproval.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tmccloud.aiapproval.entity.RolePermission;
import com.tmccloud.aiapproval.mapper.RolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolePermissionService {
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    /**
     * 获取所有角色权限
     */
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionMapper.selectList(null);
    }
    
    /**
     * 根据角色ID获取权限
     */
    public List<RolePermission> getPermissionsByRoleId(String roleId) {
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("ja_role_id", roleId);
        return rolePermissionMapper.selectList(wrapper);
    }
    
    /**
     * 保存角色权限
     */
    public void saveRolePermission(RolePermission rolePermission) {
        if (rolePermission.getJaUnid() == null) {
            rolePermissionMapper.insert(rolePermission);
        } else {
            rolePermissionMapper.updateById(rolePermission);
        }
    }
    
    /**
     * 删除角色权限
     */
    public void deleteRolePermission(Long id) {
        rolePermissionMapper.deleteById(id);
    }
    
    /**
     * 根据角色ID和权限ID删除
     */
    public void deleteByRoleAndPermission(String roleId, String permissionId) {
        QueryWrapper<RolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("ja_role_id", roleId)
               .eq("ja_permission_id", permissionId);
        rolePermissionMapper.delete(wrapper);
    }
}