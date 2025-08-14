package com.example.manus.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.manus.persistence.entity.ToolDefinition;
import com.example.manus.persistence.entity.ToolUsageLog;
import com.example.manus.persistence.entity.UserToolConfig;
import com.example.manus.persistence.mapper.ToolDefinitionMapper;
import com.example.manus.persistence.mapper.ToolUsageLogMapper;
import com.example.manus.persistence.mapper.UserToolConfigMapper;
import com.example.manus.service.PermissionService;
import com.example.manus.service.ToolManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ToolManagementServiceImpl implements ToolManagementService {

    private final ToolDefinitionMapper toolDefinitionMapper;
    private final UserToolConfigMapper userToolConfigMapper;
    private final ToolUsageLogMapper toolUsageLogMapper;
    private final PermissionService permissionService;

    @Override
    public List<ToolDefinition> getAllTools() {
        return toolDefinitionMapper.selectList(new LambdaQueryWrapper<ToolDefinition>()
                .orderByAsc(ToolDefinition::getSortOrder)
                .orderByDesc(ToolDefinition::getCreatedAt));
    }

    @Override
    public List<ToolDefinition> getEnabledTools() {
        return toolDefinitionMapper.selectEnabledTools();
    }

    @Override
    public List<ToolDefinition> getToolsByCategory(String category) {
        return toolDefinitionMapper.selectByCategory(category);
    }

    @Override
    public List<String> getToolCategories() {
        return toolDefinitionMapper.selectCategories();
    }

    @Override
    public ToolDefinition getToolByCode(String code) {
        return toolDefinitionMapper.selectByCode(code);
    }

    @Override
    public List<UserToolConfig> getUserToolConfigs(String userId) {
        return userToolConfigMapper.selectByUserId(userId);
    }

    @Override
    public List<UserToolConfig> getUserEnabledTools(String userId) {
        return userToolConfigMapper.selectEnabledByUserId(userId);
    }

    @Override
    public UserToolConfig updateUserToolConfig(String userId, String toolId, boolean enabled, String config) {
        UserToolConfig existingConfig = userToolConfigMapper.selectByUserIdAndToolId(userId, toolId);
        
        if (existingConfig != null) {
            existingConfig.setEnabled(enabled);
            existingConfig.setConfig(config);
            existingConfig.setUpdatedAt(LocalDateTime.now());
            userToolConfigMapper.updateById(existingConfig);
            return existingConfig;
        } else {
            UserToolConfig newConfig = new UserToolConfig();
            newConfig.setId(UUID.randomUUID().toString());
            newConfig.setUserId(userId);
            newConfig.setToolId(toolId);
            newConfig.setEnabled(enabled);
            newConfig.setConfig(config);
            newConfig.setCreatedAt(LocalDateTime.now());
            newConfig.setUpdatedAt(LocalDateTime.now());
            userToolConfigMapper.insert(newConfig);
            return newConfig;
        }
    }

    @Override
    public void initializeUserToolConfigs(String userId) {
        List<ToolDefinition> allTools = getEnabledTools();
        
        for (ToolDefinition tool : allTools) {
            UserToolConfig existingConfig = userToolConfigMapper.selectByUserIdAndToolId(userId, tool.getId());
            if (existingConfig == null) {
                UserToolConfig config = new UserToolConfig();
                config.setId(UUID.randomUUID().toString());
                config.setUserId(userId);
                config.setToolId(tool.getId());
                config.setEnabled(tool.getDefaultEnabled());
                config.setCreatedAt(LocalDateTime.now());
                config.setUpdatedAt(LocalDateTime.now());
                userToolConfigMapper.insert(config);
            }
        }
    }

    @Override
    public boolean hasToolPermission(String userId, String toolCode) {
        ToolDefinition tool = getToolByCode(toolCode);
        if (tool == null) {
            return false;
        }

        // 如果工具不需要权限，直接返回true
        if (!tool.getRequirePermission()) {
            return true;
        }

        // 检查用户是否有所需权限
        if (tool.getPermissionCode() != null) {
            return permissionService.hasPermission(userId, tool.getPermissionCode());
        }

        return true;
    }

    @Override
    public List<Map<String, Object>> getUserToolUsageStats(String userId, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        return toolUsageLogMapper.selectUsageStatsByUser(userId, startTime);
    }

    @Override
    public void logToolUsage(String userId, String toolId, String conversationId, String inputParams, 
                           String result, int status, long executionTime, String errorMessage) {
        ToolUsageLog log = new ToolUsageLog();
        log.setId(UUID.randomUUID().toString());
        log.setUserId(userId);
        log.setToolId(toolId);
        log.setConversationId(conversationId);
        log.setInputParams(inputParams);
        log.setResult(result);
        log.setStatus(status);
        log.setExecutionTime(executionTime);
        log.setErrorMessage(errorMessage);
        log.setCreatedAt(LocalDateTime.now());
        
        toolUsageLogMapper.insert(log);
    }
}