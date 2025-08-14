package com.example.manus.service.impl;

import com.example.manus.persistence.entity.ToolDefinition;
import com.example.manus.persistence.entity.UserToolConfig;
import com.example.manus.service.DynamicToolLoadingService;
import com.example.manus.service.ToolManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DynamicToolLoadingServiceImpl implements DynamicToolLoadingService {

    private final ApplicationContext applicationContext;
    private final ToolManagementService toolManagementService;

    @Override
    public List<Object> loadUserEnabledTools(String userId) {
        List<UserToolConfig> enabledConfigs = toolManagementService.getUserEnabledTools(userId);
        List<String> toolIds = enabledConfigs.stream()
                .map(UserToolConfig::getToolId)
                .collect(Collectors.toList());
        
        return loadToolsByIds(toolIds);
    }

    @Override
    public List<Object> loadToolsByIds(List<String> toolIds) {
        List<Object> tools = new ArrayList<>();
        
        for (String toolId : toolIds) {
            ToolDefinition toolDef = toolManagementService.getAllTools().stream()
                    .filter(tool -> tool.getId().equals(toolId))
                    .findFirst()
                    .orElse(null);
                    
            if (toolDef != null) {
                Object toolInstance = loadToolInstance(toolDef);
                if (toolInstance != null) {
                    tools.add(toolInstance);
                }
            }
        }
        
        return tools;
    }

    @Override
    public List<Object> loadToolsByCodes(List<String> toolCodes) {
        List<Object> tools = new ArrayList<>();
        
        for (String toolCode : toolCodes) {
            Object tool = loadToolByCode(toolCode);
            if (tool != null) {
                tools.add(tool);
            }
        }
        
        return tools;
    }

    @Override
    public Object loadToolByCode(String toolCode) {
        ToolDefinition toolDef = toolManagementService.getToolByCode(toolCode);
        if (toolDef == null) {
            return null;
        }
        
        return loadToolInstance(toolDef);
    }

    @Override
    public boolean isToolAvailable(String toolCode) {
        ToolDefinition toolDef = toolManagementService.getToolByCode(toolCode);
        if (toolDef == null || toolDef.getStatus() != 1) {
            return false;
        }
        
        try {
            Class<?> toolClass = Class.forName(toolDef.getBeanClass());
            return applicationContext.getBeansOfType(toolClass).size() > 0;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public List<String> getAvailableToolCodes() {
        return toolManagementService.getEnabledTools().stream()
                .filter(tool -> isToolAvailable(tool.getCode()))
                .map(ToolDefinition::getCode)
                .collect(Collectors.toList());
    }

    private Object loadToolInstance(ToolDefinition toolDef) {
        try {
            Class<?> toolClass = Class.forName(toolDef.getBeanClass());
            
            // 首先尝试从Spring上下文获取Bean
            try {
                return applicationContext.getBean(toolClass);
            } catch (Exception e) {
                // 如果Spring上下文中没有，尝试直接实例化
                return toolClass.getDeclaredConstructor().newInstance();
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load tool: " + toolDef.getCode() + " - " + e.getMessage());
            return null;
        }
    }
}