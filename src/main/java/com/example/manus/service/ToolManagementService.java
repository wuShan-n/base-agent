package com.example.manus.service;

import com.example.manus.persistence.entity.ToolDefinition;
import com.example.manus.persistence.entity.UserToolConfig;

import java.util.List;
import java.util.Map;

public interface ToolManagementService {

    List<ToolDefinition> getAllTools();

    List<ToolDefinition> getEnabledTools();

    List<ToolDefinition> getToolsByCategory(String category);

    List<String> getToolCategories();

    ToolDefinition getToolByCode(String code);

    List<UserToolConfig> getUserToolConfigs(String userId);

    List<UserToolConfig> getUserEnabledTools(String userId);

    UserToolConfig updateUserToolConfig(String userId, String toolId, boolean enabled, String config);

    void initializeUserToolConfigs(String userId);

    boolean hasToolPermission(String userId, String toolCode);

    List<Map<String, Object>> getUserToolUsageStats(String userId, int days);

    void logToolUsage(String userId, String toolId, String conversationId, String inputParams, 
                     String result, int status, long executionTime, String errorMessage);
}