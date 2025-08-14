package com.example.manus.controller;

import com.example.manus.agent.AgentFactory;
import com.example.manus.agent.Assistant;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {


    private final AgentFactory agentFactory;

    @Data
    public static class ChatRequest {
        private String conversationId; // 从前端接收会话ID
        private String message;
        private List<String> tools;    // 从前端接收启用的工具列表
    }


    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody ChatRequest request) {
        // 1. 根据请求动态创建包含了指定工具的 Assistant 实例
        Assistant assistant = agentFactory.createAssistant(request.getTools());

        // 2. 使用前端传入的 conversationId 和消息进行聊天
        return assistant.chat(request.getConversationId(), request.getMessage());
    }
}
