package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.manus.agent.AgentFactory;
import com.example.manus.agent.Assistant;
import com.example.manus.common.CommonResult;
import com.example.manus.dto.ChatRequest;
import com.example.manus.persistence.entity.AgentSession;
import com.example.manus.persistence.entity.AgentCollaboration;
import com.example.manus.service.AgentSessionService;
import com.example.manus.service.AgentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Tag(name = "多Agent系统", description = "多Agent对话和协作相关接口")
@RestController
@RequestMapping("/multi-agents")
@RequiredArgsConstructor
@SaCheckLogin
public class MultiAgentController {

    private final AgentFactory agentFactory;
    private final AgentSessionService agentSessionService;
    private final AgentManagementService agentManagementService;

    @Data
    public static class MultiAgentChatRequest extends ChatRequest {
        private String sessionId;
        private String agentId; // 指定要使用的Agent
        private String agentCode; // 或者使用Agent代码
        private String customPrompt; // 自定义系统提示词
        private List<String> collaborationAgents; // 协作Agent列表
    }

    @Data
    public static class SessionCreateRequest {
        private String sessionName;
        private String agentId;
        private String sessionType = "single"; // single, multi, collaboration
        private Map<String, Object> metadata;
    }

    @Data
    public static class CollaborationRequest {
        private String sessionId;
        private String initiatorAgentId;
        private String targetAgentId;
        private String taskType;
        private String taskDescription;
    }

    // ============== Agent对话接口 ==============

    @Operation(summary = "与指定Agent对话", description = "与特定AI助手进行对话")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatWithAgent(@RequestBody MultiAgentChatRequest request) {
        String userId = StpUtil.getLoginIdAsString();
        
        try {
            Assistant assistant;
            
            if (request.getAgentId() != null) {
                // 使用指定的Agent ID
                if (request.getCustomPrompt() != null && !request.getCustomPrompt().isEmpty()) {
                    assistant = agentFactory.createCustomAssistant(userId, request.getAgentId(), request.getCustomPrompt());
                } else {
                    assistant = agentFactory.createAssistantByAgent(userId, request.getAgentId());
                }
            } else if (request.getAgentCode() != null) {
                // 使用Agent代码
                assistant = agentFactory.createAssistantByAgentCode(userId, request.getAgentCode());
            } else if (request.getCollaborationAgents() != null && !request.getCollaborationAgents().isEmpty()) {
                // 多Agent协作模式
                String primaryAgentId = request.getCollaborationAgents().get(0);
                List<String> otherAgents = request.getCollaborationAgents().subList(1, request.getCollaborationAgents().size());
                assistant = agentFactory.createCollaborationAssistant(userId, primaryAgentId, otherAgents);
            } else {
                // 默认使用用户配置
                assistant = agentFactory.createUserAssistant(userId);
            }

            // 如果提供了sessionId，在会话上下文中进行对话
            String conversationId = request.getSessionId() != null ? 
                "session_" + request.getSessionId() : request.getConversationId();

            return assistant.chat(conversationId, request.getMessage());
            
        } catch (Exception e) {
            return Flux.error(new RuntimeException("对话失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "协作Agent对话", description = "使用多个Agent协作处理复杂任务")
    @PostMapping(value = "/collaboration-chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> collaborationChat(@RequestBody MultiAgentChatRequest request) {
        String userId = StpUtil.getLoginIdAsString();
        
        if (request.getCollaborationAgents() == null || request.getCollaborationAgents().isEmpty()) {
            return Flux.error(new RuntimeException("请指定协作Agent列表"));
        }

        try {
            String primaryAgentId = request.getCollaborationAgents().get(0);
            List<String> collaborationAgentIds = request.getCollaborationAgents().subList(1, request.getCollaborationAgents().size());
            
            Assistant assistant = agentFactory.createCollaborationAssistant(userId, primaryAgentId, collaborationAgentIds);
            
            String conversationId = request.getSessionId() != null ? 
                "collaboration_" + request.getSessionId() : request.getConversationId();
                
            return assistant.chat(conversationId, request.getMessage());
            
        } catch (Exception e) {
            return Flux.error(new RuntimeException("协作对话失败: " + e.getMessage()));
        }
    }

    // ============== 会话管理接口 ==============

    @Operation(summary = "创建会话", description = "创建新的Agent会话")
    @PostMapping("/sessions")
    public CommonResult<AgentSession> createSession(@RequestBody SessionCreateRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            if (!agentSessionService.canCreateSession(userId)) {
                return CommonResult.failed("已达到最大会话数限制");
            }

            AgentSession session = agentSessionService.createSession(
                userId, request.getSessionName(), request.getAgentId(), request.getSessionType());
            
            return CommonResult.success(session, "会话创建成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取活动会话", description = "获取用户的活动会话列表")
    @GetMapping("/sessions/active")
    public CommonResult<List<AgentSession>> getActiveSessions() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<AgentSession> sessions = agentSessionService.getActiveSessions(userId);
            return CommonResult.success(sessions);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取用户会话", description = "获取用户的会话列表")
    @GetMapping("/sessions")
    public CommonResult<List<AgentSession>> getUserSessions(
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") int limit) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<AgentSession> sessions = agentSessionService.getUserSessions(userId, limit);
            return CommonResult.success(sessions);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取会话详情", description = "获取指定会话的详细信息")
    @GetMapping("/sessions/{sessionId}")
    public CommonResult<AgentSession> getSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            AgentSession session = agentSessionService.getSession(sessionId);
            if (session == null) {
                return CommonResult.failed("会话不存在");
            }
            return CommonResult.success(session);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "切换Agent", description = "在会话中切换到不同的Agent")
    @PutMapping("/sessions/{sessionId}/switch-agent")
    public CommonResult<AgentSession> switchAgent(
            @Parameter(description = "会话ID") @PathVariable String sessionId,
            @Parameter(description = "新Agent ID") @RequestParam String agentId) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            if (!agentManagementService.hasAgentAccess(userId, agentId)) {
                return CommonResult.failed("没有权限访问该Agent");
            }

            AgentSession session = agentSessionService.switchAgent(sessionId, agentId);
            return CommonResult.success(session, "Agent切换成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "完成会话", description = "标记会话为完成状态")
    @PutMapping("/sessions/{sessionId}/complete")
    public CommonResult<Void> completeSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            agentSessionService.completeSession(sessionId);
            return CommonResult.success(null, "会话已完成");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "暂停会话", description = "暂停会话")
    @PutMapping("/sessions/{sessionId}/pause")
    public CommonResult<Void> pauseSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            agentSessionService.pauseSession(sessionId);
            return CommonResult.success(null, "会话已暂停");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "恢复会话", description = "恢复暂停的会话")
    @PutMapping("/sessions/{sessionId}/resume")
    public CommonResult<Void> resumeSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            agentSessionService.resumeSession(sessionId);
            return CommonResult.success(null, "会话已恢复");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "删除会话", description = "删除会话")
    @DeleteMapping("/sessions/{sessionId}")
    public CommonResult<Void> deleteSession(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            agentSessionService.deleteSession(sessionId);
            return CommonResult.success(null, "会话已删除");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    // ============== Agent协作接口 ==============

    @Operation(summary = "发起Agent协作", description = "在Agent之间发起协作任务")
    @PostMapping("/collaborations")
    public CommonResult<AgentCollaboration> initiateCollaboration(@RequestBody CollaborationRequest request) {
        try {
            AgentCollaboration collaboration = agentSessionService.initiateCollaboration(
                request.getSessionId(), request.getInitiatorAgentId(), 
                request.getTargetAgentId(), request.getTaskType(), request.getTaskDescription());
            
            return CommonResult.success(collaboration, "协作任务已发起");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取会话协作记录", description = "获取会话的协作记录")
    @GetMapping("/sessions/{sessionId}/collaborations")
    public CommonResult<List<AgentCollaboration>> getSessionCollaborations(
            @Parameter(description = "会话ID") @PathVariable String sessionId) {
        try {
            List<AgentCollaboration> collaborations = agentSessionService.getSessionCollaborations(sessionId);
            return CommonResult.success(collaborations);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取待处理协作任务", description = "获取Agent的待处理协作任务")
    @GetMapping("/agents/{agentId}/pending-tasks")
    public CommonResult<List<AgentCollaboration>> getPendingCollaborations(
            @Parameter(description = "Agent ID") @PathVariable String agentId) {
        try {
            List<AgentCollaboration> collaborations = agentSessionService.getPendingCollaborations(agentId);
            return CommonResult.success(collaborations);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "完成协作任务", description = "完成Agent协作任务")
    @PutMapping("/collaborations/{collaborationId}/complete")
    public CommonResult<AgentCollaboration> completeCollaboration(
            @Parameter(description = "协作ID") @PathVariable String collaborationId,
            @Parameter(description = "协作结果") @RequestParam String result) {
        try {
            AgentCollaboration collaboration = agentSessionService.completeCollaboration(collaborationId, result);
            return CommonResult.success(collaboration, "协作任务已完成");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    // ============== 统计信息接口 ==============

    @Operation(summary = "获取会话统计", description = "获取用户的会话统计信息")
    @GetMapping("/sessions/statistics")
    public CommonResult<Map<String, Object>> getSessionStatistics() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            Map<String, Object> stats = agentSessionService.getSessionStatistics(userId);
            return CommonResult.success(stats);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取活动会话数", description = "获取用户当前活动的会话数量")
    @GetMapping("/sessions/active-count")
    public CommonResult<Integer> getActiveSessionCount() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            int count = agentSessionService.getActiveSessionCount(userId);
            return CommonResult.success(count);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}