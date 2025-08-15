package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.manus.agent.AgentFactory;
import com.example.manus.agent.Assistant;
import com.example.manus.dto.ChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

//@Tag(name = "AI助手", description = "AI对话助手相关接口")
@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
@SaCheckLogin
public class AssistantController {

    private final AgentFactory agentFactory;



    @Operation(summary = "AI对话", description = "与AI助手进行对话，支持工具调用和Agent选择")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        String userId = StpUtil.getLoginIdAsString();

        try {
            Assistant assistant;

            // 优先检查是否指定了Agent
            if (request.getAgentId() != null && !request.getAgentId().isEmpty()) {
                assistant = agentFactory.createAssistantByAgent(userId, request.getAgentId());
            } else if (request.getAgentCode() != null && !request.getAgentCode().isEmpty()) {
                assistant = agentFactory.createAssistantByAgentCode(userId, request.getAgentCode());
            } else if (request.isUseUserConfig() || (request.getTools() == null || request.getTools().isEmpty())) {
                // 使用用户配置的工具
                assistant = agentFactory.createUserAssistant(userId);
            } else {
                // 使用请求指定的工具，但需要验证权限
                assistant = agentFactory.createAssistantForUser(userId, request.getTools());
            }

            return assistant.chat(request.getConversationId(), request.getMessage());

        } catch (Exception e) {
            return Flux.error(new RuntimeException("对话失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "快速对话", description = "使用用户默认工具配置进行对话")
    @PostMapping(value = "/quick-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> quickChat(@RequestBody ChatRequest request) {
        String userId = StpUtil.getLoginIdAsString();
        Assistant assistant = agentFactory.createUserAssistant(userId);
        return assistant.chat(request.getConversationId(), request.getMessage());
    }
}
