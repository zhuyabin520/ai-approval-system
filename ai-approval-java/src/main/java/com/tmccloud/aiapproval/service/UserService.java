package com.tmccloud.aiapproval.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tmccloud.aiapproval.entity.User;
import com.tmccloud.aiapproval.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return userMapper.selectOne(wrapper);
    }
    
    /**
     * 保存用户
     */
    public void saveUser(User user) {
        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.updateById(user);
        }
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    /**
     * 根据角色获取用户
     */
    public List<User> getUsersByRole(String role) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("role", role);
        return userMapper.selectList(wrapper);
    }
}