package com.example.manus.service;

import com.example.manus.persistence.entity.AgentDefinition;
import com.example.manus.persistence.entity.UserAgentConfig;
import com.example.manus.persistence.entity.AgentToolConfig;

import java.util.List;
import java.util.Map;

public interface AgentManagementService {

    // Agent定义管理
    List<AgentDefinition> getAllAgents();
    
    List<AgentDefinition> getEnabledAgents();
    
    List<AgentDefinition> getAgentsByCategory(String category);
    
    AgentDefinition getAgentById(String agentId);
    
    AgentDefinition getAgentByCode(String code);
    
    List<String> getAgentCategories();
    
    List<AgentDefinition> getSystemAgents();
    
    AgentDefinition createAgent(AgentDefinition agent);
    
    AgentDefinition updateAgent(AgentDefinition agent);
    
    void deleteAgent(String agentId);

    // 用户Agent配置管理
    List<UserAgentConfig> getUserEnabledAgents(String userId);
    
    List<UserAgentConfig> getUserFavoriteAgents(String userId);
    
    UserAgentConfig getUserAgentConfig(String userId, String agentId);
    
    UserAgentConfig updateUserAgentConfig(String userId, String agentId, Boolean enabled, 
                                         String customName, String customPrompt, Boolean isFavorite);
    
    void initializeUserAgentConfigs(String userId);
    
    void incrementAgentUsage(String userId, String agentId);

    // Agent工具配置管理
    List<AgentToolConfig> getAgentToolConfigs(String agentId);
    
    List<AgentToolConfig> getEnabledAgentTools(String agentId);
    
    AgentToolConfig updateAgentToolConfig(String agentId, String toolId, Boolean enabled, String config, Integer priority);
    
    void initializeAgentToolConfigs(String agentId);

    // 权限验证
    boolean hasAgentAccess(String userId, String agentId);
    
    boolean canCreateAgent(String userId);

    // 统计信息
    Map<String, Object> getAgentStatistics(String userId);
    
    List<Map<String, Object>> getUserAgentUsageStats(String userId, int days);
}