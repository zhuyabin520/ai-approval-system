package com.tmccloud.aiapproval.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tmccloud.aiapproval.entity.AISettings;
import com.tmccloud.aiapproval.mapper.AISettingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AISettingsService {
    
    @Autowired
    private AISettingsMapper aiSettingsMapper;
    
    /**
     * 获取当前启用的AI设置
     */
    public AISettings getCurrentSettings() {
        QueryWrapper<AISettings> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", true).orderByDesc("updated_at").last("LIMIT 1");
        return aiSettingsMapper.selectOne(wrapper);
    }
    
    /**
     * 获取所有AI设置
     */
    public List<AISettings> getAllSettings() {
        return aiSettingsMapper.selectList(null);
    }
    
    /**
     * 根据ID获取AI设置
     */
    public AISettings getSettingsById(Long id) {
        return aiSettingsMapper.selectById(id);
    }
    
    /**
     * 保存或更新AI设置
     */
    public void saveSettings(AISettings settings) {
        if (settings.getId() == null) {
            settings.setCreatedAt(LocalDateTime.now());
            settings.setUpdatedAt(LocalDateTime.now());
            aiSettingsMapper.insert(settings);
        } else {
            settings.setUpdatedAt(LocalDateTime.now());
            aiSettingsMapper.updateById(settings);
        }
    }
    
    /**
     * 删除AI设置
     */
    public void deleteSettings(Long id) {
        aiSettingsMapper.deleteById(id);
    }
    
    /**
     * 启用指定的AI设置
     */
    public void enableSettings(Long id) {
        // 先禁用所有设置
        AISettings update = new AISettings();
        update.setEnabled(false);
        aiSettingsMapper.update(update, null);
        
        // 启用指定设置
        AISettings settings = new AISettings();
        settings.setId(id);
        settings.setEnabled(true);
        settings.setUpdatedAt(LocalDateTime.now());
        aiSettingsMapper.updateById(settings);
    }
}