package com.example.manus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.manus.persistence.entity.AgentDefinition;
import com.example.manus.persistence.entity.AgentToolConfig;
import com.example.manus.persistence.entity.ToolDefinition;
import com.example.manus.persistence.entity.UserAgentConfig;
import com.example.manus.persistence.mapper.AgentDefinitionMapper;
import com.example.manus.persistence.mapper.AgentToolConfigMapper;
import com.example.manus.persistence.mapper.ToolDefinitionMapper;
import com.example.manus.persistence.mapper.UserAgentConfigMapper;
import com.example.manus.service.AgentManagementService;
import com.example.manus.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AgentManagementServiceImpl implements AgentManagementService {

    private final AgentDefinitionMapper agentDefinitionMapper;
    private final UserAgentConfigMapper userAgentConfigMapper;
    private final AgentToolConfigMapper agentToolConfigMapper;
    private final ToolDefinitionMapper toolDefinitionMapper;
    private final PermissionService permissionService;

    @Override
    public List<AgentDefinition> getAllAgents() {
        return agentDefinitionMapper.selectList(
            new QueryWrapper<AgentDefinition>().orderByAsc("category", "name")
        );
    }

    @Override
    public List<AgentDefinition> getEnabledAgents() {
        return agentDefinitionMapper.selectEnabledAgents();
    }

    @Override
    public List<AgentDefinition> getAgentsByCategory(String category) {
        return agentDefinitionMapper.selectByCategory(category);
    }

    @Override
    public AgentDefinition getAgentById(String agentId) {
        return agentDefinitionMapper.selectById(agentId);
    }

    @Override
    public AgentDefinition getAgentByCode(String code) {
        return agentDefinitionMapper.selectByCode(code);
    }

    @Override
    public List<String> getAgentCategories() {
        return agentDefinitionMapper.selectCategories();
    }

    @Override
    public List<AgentDefinition> getSystemAgents() {
        return agentDefinitionMapper.selectSystemAgents();
    }

    @Override
    public AgentDefinition createAgent(AgentDefinition agent) {
        agent.setCreatedAt(LocalDateTime.now());
        agent.setUpdatedAt(LocalDateTime.now());
        agent.setStatus(AgentDefinition.Status.ENABLED);
        agentDefinitionMapper.insert(agent);

        // 初始化Agent工具配置
        initializeAgentToolConfigs(agent.getId());

        return agent;
    }

    @Override
    public AgentDefinition updateAgent(AgentDefinition agent) {
        agent.setUpdatedAt(LocalDateTime.now());
        agentDefinitionMapper.updateById(agent);
        return agent;
    }

    @Override
    public void deleteAgent(String agentId) {
        AgentDefinition agent = agentDefinitionMapper.selectById(agentId);
        if (agent != null && !agent.getIsSystemAgent()) {
            agentDefinitionMapper.deleteById(agentId);
        }
    }

    @Override
    public List<UserAgentConfig> getUserEnabledAgents(String userId) {
        return userAgentConfigMapper.selectEnabledByUserId(userId);
    }

    @Override
    public List<UserAgentConfig> getUserFavoriteAgents(String userId) {
        return userAgentConfigMapper.selectFavoritesByUserId(userId);
    }

    @Override
    public UserAgentConfig getUserAgentConfig(String userId, String agentId) {
        return userAgentConfigMapper.selectByUserAndAgent(userId, agentId);
    }

    @Override
    public UserAgentConfig updateUserAgentConfig(String userId, String agentId, Boolean enabled,
                                               String customName, String customPrompt, Boolean isFavorite) {
        UserAgentConfig config = userAgentConfigMapper.selectByUserAndAgent(userId, agentId);

        if (config == null) {
            config = new UserAgentConfig();
            config.setUserId(userId);
            config.setAgentId(agentId);
            config.setEnabled(enabled != null ? enabled : true);
            config.setCustomName(customName);
            config.setCustomPrompt(customPrompt);
            config.setIsFavorite(isFavorite != null ? isFavorite : false);
            config.setUsageCount(0);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            userAgentConfigMapper.insert(config);
        } else {
            if (enabled != null) config.setEnabled(enabled);
            if (customName != null) config.setCustomName(customName);
            if (customPrompt != null) config.setCustomPrompt(customPrompt);
            if (isFavorite != null) config.setIsFavorite(isFavorite);
            config.setUpdatedAt(LocalDateTime.now());
            userAgentConfigMapper.updateById(config);
        }

        return config;
    }

    @Override
    public void initializeUserAgentConfigs(String userId) {
        List<AgentDefinition> systemAgents = getSystemAgents();

        for (AgentDefinition agent : systemAgents) {
            UserAgentConfig existingConfig = userAgentConfigMapper.selectByUserAndAgent(userId, agent.getId());
            if (existingConfig == null) {
                UserAgentConfig config = new UserAgentConfig();
                config.setUserId(userId);
                config.setAgentId(agent.getId());
                config.setEnabled(true);
                config.setIsFavorite("GENERAL_ASSISTANT".equals(agent.getCode()));
                config.setUsageCount(0);
                config.setCreatedAt(LocalDateTime.now());
                config.setUpdatedAt(LocalDateTime.now());
                userAgentConfigMapper.insert(config);
            }
        }
    }

    @Override
    public void incrementAgentUsage(String userId, String agentId) {
        userAgentConfigMapper.incrementUsageCount(userId, agentId);
    }

    @Override
    public List<AgentToolConfig> getAgentToolConfigs(String agentId) {
        return agentToolConfigMapper.selectList(
            new QueryWrapper<AgentToolConfig>()
                .eq("agent_id", agentId)
                .orderByAsc("priority")
        );
    }

    @Override
    public List<AgentToolConfig> getEnabledAgentTools(String agentId) {
        return agentToolConfigMapper.selectEnabledByAgentId(agentId);
    }

    @Override
    public AgentToolConfig updateAgentToolConfig(String agentId, String toolId, Boolean enabled, String config, Integer priority) {
        AgentToolConfig toolConfig = agentToolConfigMapper.selectByAgentAndTool(agentId, toolId);

        if (toolConfig == null) {
            toolConfig = new AgentToolConfig();
            toolConfig.setAgentId(agentId);
            toolConfig.setToolId(toolId);
            toolConfig.setEnabled(enabled != null ? enabled : true);
            toolConfig.setConfig(config);
            toolConfig.setPriority(priority != null ? priority : 0);
            toolConfig.setCreatedAt(LocalDateTime.now());
            toolConfig.setUpdatedAt(LocalDateTime.now());
            agentToolConfigMapper.insert(toolConfig);
        } else {
            if (enabled != null) toolConfig.setEnabled(enabled);
            if (config != null) toolConfig.setConfig(config);
            if (priority != null) toolConfig.setPriority(priority);
            toolConfig.setUpdatedAt(LocalDateTime.now());
            agentToolConfigMapper.updateById(toolConfig);
        }

        return toolConfig;
    }

    @Override
    public void initializeAgentToolConfigs(String agentId) {
        AgentDefinition agent = getAgentById(agentId);
        if (agent == null) return;

        List<ToolDefinition> availableTools = toolDefinitionMapper.selectEnabledTools();
        Map<String, Integer> toolPriorities = getDefaultToolPriorities(agent.getCode());

        for (ToolDefinition tool : availableTools) {
            AgentToolConfig existingConfig = agentToolConfigMapper.selectByAgentAndTool(agentId, tool.getId());
            if (existingConfig == null) {
                boolean shouldEnable = shouldEnableToolForAgent(agent.getCode(), tool.getCode());
                if (shouldEnable) {
                    AgentToolConfig config = new AgentToolConfig();
                    config.setAgentId(agentId);
                    config.setToolId(tool.getId());
                    config.setEnabled(true);
                    config.setPriority(toolPriorities.getOrDefault(tool.getCode(), 5));
                    config.setCreatedAt(LocalDateTime.now());
                    config.setUpdatedAt(LocalDateTime.now());
                    agentToolConfigMapper.insert(config);
                }
            }
        }
    }

    @Override
    public boolean hasAgentAccess(String userId, String agentId) {
        AgentDefinition agent = getAgentById(agentId);
        if (agent == null || agent.getStatus() != AgentDefinition.Status.ENABLED) {
            return false;
        }

        // 系统Agent默认所有用户都可以访问
        if (agent.getIsSystemAgent()) {
            return true;
        }

        // 检查是否是Agent创建者
        if (userId.equals(agent.getCreatedBy())) {
            return true;
        }

        // 检查用户权限
        return permissionService.hasPermission(userId);
    }

    @Override
    public boolean canCreateAgent(String userId) {
        return permissionService.hasPermission(userId);
    }

    @Override
    public Map<String, Object> getAgentStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalAgents", agentDefinitionMapper.selectList(null).size());
        stats.put("enabledAgents", getEnabledAgents().size());
        stats.put("userEnabledAgents", userAgentConfigMapper.countEnabledByUserId(userId));
        stats.put("userFavoriteAgents", userAgentConfigMapper.countFavoritesByUserId(userId));
        stats.put("categories", getAgentCategories().size());

        return stats;
    }

    @Override
    public List<Map<String, Object>> getUserAgentUsageStats(String userId, int days) {
        // 这里可以实现更复杂的统计逻辑
        // 目前返回基本的使用统计
        List<UserAgentConfig> configs = getUserEnabledAgents(userId);

        return configs.stream()
            .map(config -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("agentId", config.getAgentId());
                stat.put("usageCount", config.getUsageCount());
                stat.put("lastUsedAt", config.getLastUsedAt());
                return stat;
            })
            .collect(Collectors.toList());
    }

    private boolean shouldEnableToolForAgent(String agentCode, String toolCode) {
        switch (agentCode) {
            case "CODE_ASSISTANT":
                return List.of("TERMINAL", "WEB_SCRAPER", "CALCULATOR").contains(toolCode);
            case "WRITING_ASSISTANT":
                return List.of("KNOWLEDGE_BASE", "WEB_SCRAPER").contains(toolCode);
            case "DATA_ANALYST":
                return List.of("CALCULATOR", "WEB_SCRAPER").contains(toolCode);
            case "RESEARCH_ASSISTANT":
                return List.of("KNOWLEDGE_BASE", "WEB_SCRAPER").contains(toolCode);
            case "CUSTOMER_SUPPORT":
                return List.of("KNOWLEDGE_BASE").contains(toolCode);
            case "GENERAL_ASSISTANT":
                return true; // 通用助手启用所有工具
            default:
                return false;
        }
    }

    private Map<String, Integer> getDefaultToolPriorities(String agentCode) {
        Map<String, Integer> priorities = new HashMap<>();

        switch (agentCode) {
            case "CODE_ASSISTANT":
                priorities.put("TERMINAL", 1);
                priorities.put("WEB_SCRAPER", 2);
                priorities.put("CALCULATOR", 3);
                break;
            case "DATA_ANALYST":
                priorities.put("CALCULATOR", 1);
                priorities.put("WEB_SCRAPER", 2);
                break;
            case "RESEARCH_ASSISTANT":
                priorities.put("KNOWLEDGE_BASE", 1);
                priorities.put("WEB_SCRAPER", 2);
                break;
            case "CUSTOMER_SUPPORT":
                priorities.put("KNOWLEDGE_BASE", 1);
                break;
            default:
                priorities.put("CALCULATOR", 3);
                priorities.put("WEB_SCRAPER", 4);
                priorities.put("KNOWLEDGE_BASE", 2);
                priorities.put("TERMINAL", 5);
                break;
        }

        return priorities;
    }
}
