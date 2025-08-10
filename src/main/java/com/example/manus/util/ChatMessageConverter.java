package com.example.manus.util;

import com.example.manus.persistence.entity.ChatMessage;
import com.example.manus.persistence.entity.ChatMessageRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatMessageConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ChatMessage toEntity(dev.langchain4j.data.message.ChatMessage langchainMessage, String conversationId) {
        ChatMessage entity = new ChatMessage();
        entity.setId(UUID.randomUUID().toString());
        entity.setConversationId(conversationId);
        switch (langchainMessage) {
            case AiMessage aiMessage -> {
                entity.setRole(ChatMessageRole.AI);
                if (aiMessage.hasToolExecutionRequests()) {
                    ArrayNode requestsNode = objectMapper.createArrayNode();
                    for (ToolExecutionRequest request : aiMessage.toolExecutionRequests()) {
                        ObjectNode requestNode = objectMapper.createObjectNode()
                                .put("id", request.id())
                                .put("name", request.name())
                                .put("arguments", request.arguments());
                        requestsNode.add(requestNode);
                    }
                    entity.setContent(requestsNode.toPrettyString());
                } else {
                    entity.setContent(aiMessage.text() == null ? "" : aiMessage.text());
                }
            }
            case UserMessage userMessage -> {
                entity.setRole(ChatMessageRole.USER);
                entity.setContent(userMessage.singleText());
            }
            case SystemMessage systemMessage -> {
                entity.setRole(ChatMessageRole.SYSTEM);
                entity.setContent(systemMessage.text());
            }
            case ToolExecutionResultMessage toolMessage -> {
                entity.setRole(ChatMessageRole.TOOL_EXECUTION_RESULT);
                ObjectNode resultNode = objectMapper.createObjectNode()
                        .put("id", toolMessage.id())
                        .put("toolName", toolMessage.toolName())
                        .put("resultText", toolMessage.text());
                entity.setContent(resultNode.toPrettyString());
            }
            default -> {
                entity.setRole(ChatMessageRole.SYSTEM);
                entity.setContent(langchainMessage.toString());
            }
        }
        return entity;
    }

    public static dev.langchain4j.data.message.ChatMessage fromEntity(ChatMessage entity) {
        String content = entity.getContent();

        switch (entity.getRole()) {
            case USER:
                return UserMessage.from(content);
            case AI:
                // 尝试将 content 解析为工具调用请求
                try {
                    JsonNode contentNode = objectMapper.readTree(content);
                    if (contentNode.isArray()) {
                        List<ToolExecutionRequest> requests = new ArrayList<>();
                        for (JsonNode requestNode : contentNode) {
                            ToolExecutionRequest request = ToolExecutionRequest.builder()
                                    .id(requestNode.path("id").asText())
                                    .name(requestNode.path("name").asText())
                                    .arguments(requestNode.path("arguments").asText())
                                    .build();
                            requests.add(request);
                        }
                        if (!requests.isEmpty()) {
                            return AiMessage.from(requests);
                        }
                    }
                } catch (JsonProcessingException e) {
                    // 如果解析失败，说明它不是一个JSON，而是普通的文本回复
                }
                return AiMessage.from(content); // 作为普通文本消息返回
            case SYSTEM:
                return SystemMessage.from(content);
            case TOOL_EXECUTION_RESULT:
                try {
                    JsonNode resultNode = objectMapper.readTree(content);
                    return ToolExecutionResultMessage.from(
                            resultNode.path("id").asText("unknown_id"),
                            resultNode.path("toolName").asText("unknown_tool"),
                            resultNode.path("resultText").asText("")
                    );
                } catch (JsonProcessingException e) {
                    // 如果解析失败，提供一个安全的备用方案
                    System.err.println("无法从 content 解析工具执行结果: " + content);
                    return ToolExecutionResultMessage.from("parse_error", "parse_error", content);
                }
            default:
                throw new IllegalArgumentException("Unknown role: " + entity.getRole());
        }
    }

    public static List<dev.langchain4j.data.message.ChatMessage> fromEntities(List<ChatMessage> entities) {
        return entities.stream().map(ChatMessageConverter::fromEntity).collect(Collectors.toList());
    }
}
