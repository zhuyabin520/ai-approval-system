package com.tmccloud.aiapproval.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tmccloud.aiapproval.entity.ProcessSetting;
import com.tmccloud.aiapproval.mapper.ProcessSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessSettingService {
    
    @Autowired
    private ProcessSettingMapper processSettingMapper;
    
    /**
     * 获取所有流程设置
     */
    public List<ProcessSetting> getAllProcessSettings() {
        return processSettingMapper.selectList(null);
    }
    
    /**
     * 根据流程ID获取流程步骤
     */
    public List<ProcessSetting> getStepsByProcessId(String processId) {
        QueryWrapper<ProcessSetting> wrapper = new QueryWrapper<>();
        wrapper.eq("le_process_id", processId)
               .orderByAsc("le_step_order");
        return processSettingMapper.selectList(wrapper);
    }
    
    /**
     * 保存流程设置
     */
    public void saveProcessSetting(ProcessSetting processSetting) {
        if (processSetting.getLeUnid() == null) {
            processSettingMapper.insert(processSetting);
        } else {
            processSettingMapper.updateById(processSetting);
        }
    }
    
    /**
     * 删除流程设置
     */
    public void deleteProcessSetting(Long id) {
        processSettingMapper.deleteById(id);
    }
    
    /**
     * 根据流程ID和步骤ID删除
     */
    public void deleteByProcessAndStep(String processId, String stepId) {
        QueryWrapper<ProcessSetting> wrapper = new QueryWrapper<>();
        wrapper.eq("le_process_id", processId)
               .eq("le_step_id", stepId);
        processSettingMapper.delete(wrapper);
    }
}