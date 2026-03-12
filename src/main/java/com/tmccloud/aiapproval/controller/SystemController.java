package com.tmccloud.aiapproval.controller;

import com.tmccloud.aiapproval.entity.AISettings;
import com.tmccloud.aiapproval.entity.User;
import com.tmccloud.aiapproval.service.AISettingsService;
import com.tmccloud.aiapproval.service.UserService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    @Autowired
    private RepositoryService repositoryService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AISettingsService aiSettingsService;
    
    /**
     * 获取系统设置
     */
    @GetMapping("/settings")
    public Map<String, Object> getSystemSettings() {
        Map<String, Object> settings = new java.util.HashMap<>();
        settings.put("systemName", "智能审批系统");
        settings.put("processDefinition", "");
        settings.put("approverConfig", "");
        return settings;
    }
    
    /**
     * 更新系统设置
     */
    @PutMapping("/settings")
    public void updateSystemSettings(@RequestBody Map<String, Object> settings) {
        // 这里可以保存设置到数据库
        System.out.println("更新系统设置: " + settings);
    }
    
    /**
     * 获取 AI 设置
     */
    @GetMapping("/ai-settings")
    public AISettings getAISettings() {
        AISettings settings = aiSettingsService.getCurrentSettings();
        if (settings == null) {
            // 如果没有设置，返回默认值
            settings = new AISettings();
            settings.setModelName("qwen3.5-flash");
            settings.setBaseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1");
            settings.setTemperature(new java.math.BigDecimal(0.7));
            settings.setMaxTokens(2048);
        }
        return settings;
    }
    
    /**
     * 更新 AI 设置
     */
    @PutMapping("/ai-settings")
    public void updateAISettings(@RequestBody AISettings settings) {
        aiSettingsService.saveSettings(settings);
        // 启用该设置
        aiSettingsService.enableSettings(settings.getId());
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    
    /**
     * 保存用户
     */
    @PostMapping("/users")
    public void saveUser(@RequestBody User user) {
        userService.saveUser(user);
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    
    /**
     * 获取流程定义列表
     */
    @GetMapping("/process-definitions")
    public List<Map<String, Object>> getProcessDefinitions() {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .list();
        
        return processDefinitions.stream().map(process -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", process.getId());
            map.put("name", process.getName());
            map.put("key", process.getKey());
            map.put("version", process.getVersion());
            return map;
        }).collect(Collectors.toList());
    }
    
    /**
     * 部署流程
     */
    @PostMapping("/process-deploy")
    public void deployProcess(@RequestParam("file") MultipartFile file) {
        try {
            repositoryService.createDeployment()
                    .addBytes(file.getOriginalFilename(), file.getBytes())
                    .deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}