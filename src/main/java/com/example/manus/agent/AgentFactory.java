package com.example.manus.agent;

import cn.dev33.satoken.stp.StpUtil;
import com.example.manus.persistence.entity.AgentDefinition;
import com.example.manus.persistence.entity.AgentToolConfig;
import com.example.manus.service.AgentManagementService;
import com.example.manus.service.DynamicToolLoadingService;
import com.example.manus.service.ToolManagementService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class AgentFactory {

    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryProvider chatMemoryProvider;
    private final ApplicationContext context;
    private final DynamicToolLoadingService dynamicToolLoadingService;
    private final ToolManagementService toolManagementService;
    private final AgentManagementService agentManagementService;

    @Bean
    public Assistant assistant() {
        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    /**
     * 基于用户配置动态创建Assistant实例
     * @param userId 用户ID，如果为null则使用当前登录用户
     * @return 包含用户启用工具的Assistant实例
     */
    public Assistant createUserAssistant(String userId) {
        if (userId == null) {
            try {
                userId = StpUtil.getLoginIdAsString();
            } catch (Exception e) {
                // 如果无法获取用户ID，返回不包含工具的Assistant
                return assistant();
            }
        }

        List<Object> userTools = dynamicToolLoadingService.loadUserEnabledTools(userId);

        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(userTools)
                .build();
    }

    /**
     * 为特定用户创建Assistant，支持工具权限验证
     * @param userId 用户ID
     * @param requestedToolCodes 请求的工具代码列表
     * @return Assistant实例
     */
    public Assistant createAssistantForUser(String userId, List<String> requestedToolCodes) {
        List<Object> allowedTools = new ArrayList<>();

        if (requestedToolCodes != null) {
            for (String toolCode : requestedToolCodes) {
                // 检查用户权限
                if (toolManagementService.hasToolPermission(userId, toolCode)) {
                    Object tool = dynamicToolLoadingService.loadToolByCode(toolCode);
                    if (tool != null) {
                        allowedTools.add(tool);
                    }
                }
            }
        }

        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(allowedTools)
                .build();
    }

    /**
     * 基于Agent定义创建Assistant实例
     * @param userId 用户ID
     * @param agentId Agent定义ID
     * @return 定制化的Assistant实例
     */
    public Assistant createAssistantByAgent(String userId, String agentId) {
        AgentDefinition agent = agentManagementService.getAgentById(agentId);
        if (agent == null || agent.getStatus() != AgentDefinition.Status.ENABLED) {
            throw new RuntimeException("Agent不存在或已禁用");
        }

        // 获取Agent的工具配置
        List<AgentToolConfig> toolConfigs = agentManagementService.getEnabledAgentTools(agentId);
        List<Object> agentTools = new ArrayList<>();

        for (AgentToolConfig toolConfig : toolConfigs) {
            Object tool = dynamicToolLoadingService.loadToolByCode(getToolCodeById(toolConfig.getToolId()));
            if (tool != null) {
                agentTools.add(tool);
            }
        }

        // 构建Assistant，应用Agent特定的配置
        AiServices<Assistant> builder = AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(agentTools);

        // 应用Agent的系统提示词
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isEmpty()) {
            builder.systemMessageProvider(memoryId -> agent.getSystemPrompt());
        }

        return builder.build();
    }

    /**
     * 基于Agent代码创建Assistant实例
     * @param userId 用户ID
     * @param agentCode Agent代码
     * @return Assistant实例
     */
    public Assistant createAssistantByAgentCode(String userId, String agentCode) {
        AgentDefinition agent = agentManagementService.getAgentByCode(agentCode);
        if (agent == null) {
            throw new RuntimeException("Agent不存在: " + agentCode);
        }
        return createAssistantByAgent(userId, agent.getId());
    }

    /**
     * 创建带有自定义系统提示词的Assistant
     * @param userId 用户ID
     * @param agentId Agent ID
     * @param customPrompt 自定义系统提示词
     * @return Assistant实例
     */
    public Assistant createCustomAssistant(String userId, String agentId, String customPrompt) {
        AgentDefinition agent = agentManagementService.getAgentById(agentId);
        if (agent == null || agent.getStatus() != AgentDefinition.Status.ENABLED) {
            throw new RuntimeException("Agent不存在或已禁用");
        }



        // 获取Agent的工具配置
        List<AgentToolConfig> toolConfigs = agentManagementService.getEnabledAgentTools(agentId);
        List<Object> agentTools = new ArrayList<>();

        for (AgentToolConfig toolConfig : toolConfigs) {
            Object tool = dynamicToolLoadingService.loadToolByCode(getToolCodeById(toolConfig.getToolId()));
            if (tool != null) {
                agentTools.add(tool);
            }
        }

        // 使用自定义提示词
        String finalPrompt = customPrompt != null && !customPrompt.isEmpty()
            ? customPrompt
            : agent.getSystemPrompt();

        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(agentTools)
                .systemMessageProvider(memoryId -> finalPrompt)
                .build();
    }

    /**
     * 创建多Agent协作的Assistant
     * @param userId 用户ID
     * @param primaryAgentId 主要Agent ID
     * @param collaborationAgentIds 协作Agent ID列表
     * @return Assistant实例
     */
    public Assistant createCollaborationAssistant(String userId, String primaryAgentId, List<String> collaborationAgentIds) {
        // 主要Agent
        AgentDefinition primaryAgent = agentManagementService.getAgentById(primaryAgentId);
        if (primaryAgent == null || !agentManagementService.hasAgentAccess(userId, primaryAgentId)) {
            throw new RuntimeException("无法访问主要Agent");
        }

        // 收集所有Agent的工具
        List<Object> allTools = new ArrayList<>();
        List<AgentToolConfig> primaryTools = agentManagementService.getEnabledAgentTools(primaryAgentId);

        for (AgentToolConfig toolConfig : primaryTools) {
            Object tool = dynamicToolLoadingService.loadToolByCode(getToolCodeById(toolConfig.getToolId()));
            if (tool != null) {
                allTools.add(tool);
            }
        }

        // 添加协作Agent的工具（去重）
        if (collaborationAgentIds != null) {
            for (String agentId : collaborationAgentIds) {
                if (agentManagementService.hasAgentAccess(userId, agentId)) {
                    List<AgentToolConfig> collaborationTools = agentManagementService.getEnabledAgentTools(agentId);
                    for (AgentToolConfig toolConfig : collaborationTools) {
                        Object tool = dynamicToolLoadingService.loadToolByCode(getToolCodeById(toolConfig.getToolId()));
                        if (tool != null && !containsTool(allTools, tool)) {
                            allTools.add(tool);
                        }
                    }
                }
            }
        }

        // 构建协作提示词
        String collaborationPrompt = buildCollaborationPrompt(primaryAgent, collaborationAgentIds);

        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(allTools)
                .systemMessageProvider(memoryId -> collaborationPrompt)
                .build();
    }

    private String getToolCodeById(String toolId) {
        return toolManagementService.getAllTools().stream()
                .filter(tool -> tool.getId().equals(toolId))
                .findFirst()
                .map(tool -> tool.getCode())
                .orElse(null);
    }

    private boolean containsTool(List<Object> tools, Object newTool) {
        return tools.stream()
                .anyMatch(tool -> tool.getClass().equals(newTool.getClass()));
    }

    private String buildCollaborationPrompt(AgentDefinition primaryAgent, List<String> collaborationAgentIds) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个协作AI助手系统。");
        prompt.append("主要Agent: ").append(primaryAgent.getDisplayName()).append(" - ").append(primaryAgent.getDescription());
        prompt.append("\n").append(primaryAgent.getSystemPrompt());

        if (collaborationAgentIds != null && !collaborationAgentIds.isEmpty()) {
            prompt.append("\n\n你还可以与以下专业助手协作：");
            for (String agentId : collaborationAgentIds) {
                AgentDefinition agent = agentManagementService.getAgentById(agentId);
                if (agent != null) {
                    prompt.append("\n- ").append(agent.getDisplayName()).append(": ").append(agent.getDescription());
                }
            }
        }

        prompt.append("\n\n请根据用户需求选择最适合的方式来处理任务，必要时可以结合多个专业领域的知识来提供最佳解决方案。");

        return prompt.toString();
    }
}
