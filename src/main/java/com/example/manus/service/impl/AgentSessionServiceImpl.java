package com.example.manus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.manus.persistence.entity.AgentCollaboration;
import com.example.manus.persistence.entity.AgentSession;
import com.example.manus.persistence.mapper.AgentCollaborationMapper;
import com.example.manus.persistence.mapper.AgentSessionMapper;
import com.example.manus.service.AgentSessionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AgentSessionServiceImpl implements AgentSessionService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentCollaborationMapper agentCollaborationMapper;
    private final ObjectMapper objectMapper;

    private static final int MAX_ACTIVE_SESSIONS_PER_USER = 10;

    @Override
    public AgentSession createSession(String userId, String sessionName, String agentId, String sessionType) {
        // 检查是否超过最大活动会话数
        if (getActiveSessionCount(userId) >= MAX_ACTIVE_SESSIONS_PER_USER) {
            throw new RuntimeException("已达到最大活动会话数限制");
        }

        AgentSession session = new AgentSession();
        session.setUserId(userId);
        session.setSessionName(sessionName);
        session.setCurrentAgentId(agentId);
        session.setSessionType(sessionType != null ? sessionType : AgentSession.Type.SINGLE);
        session.setStatus(AgentSession.Status.ACTIVE);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        // 设置会话过期时间（默认24小时）
        session.setExpiresAt(LocalDateTime.now().plusHours(24));

        agentSessionMapper.insert(session);
        return session;
    }

    @Override
    public AgentSession getSession(String sessionId) {
        return agentSessionMapper.selectById(sessionId);
    }

    @Override
    public List<AgentSession> getActiveSessions(String userId) {
        return agentSessionMapper.selectActiveByUserId(userId);
    }

    @Override
    public List<AgentSession> getUserSessions(String userId, int limit) {
        return agentSessionMapper.selectRecentByUserId(userId, limit);
    }

    @Override
    public AgentSession switchAgent(String sessionId, String agentId) {
        agentSessionMapper.switchAgent(sessionId, agentId);
        return getSession(sessionId);
    }

    @Override
    public AgentSession updateSession(String sessionId, String sessionName, Map<String, Object> metadata) {
        UpdateWrapper<AgentSession> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", sessionId);

        if (sessionName != null) {
            updateWrapper.set("session_name", sessionName);
        }

        if (metadata != null) {
            try {
                String metadataJson = objectMapper.writeValueAsString(metadata);
                updateWrapper.set("metadata", metadataJson);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize session metadata", e);
            }
        }

        updateWrapper.set("updated_at", LocalDateTime.now());
        agentSessionMapper.update(null, updateWrapper);

        return getSession(sessionId);
    }

    @Override
    public void completeSession(String sessionId) {
        agentSessionMapper.completeSession(sessionId);
    }

    @Override
    public void pauseSession(String sessionId) {
        UpdateWrapper<AgentSession> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", sessionId)
                    .set("status", AgentSession.Status.PAUSED)
                    .set("updated_at", LocalDateTime.now());
        agentSessionMapper.update(null, updateWrapper);
    }

    @Override
    public void resumeSession(String sessionId) {
        UpdateWrapper<AgentSession> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", sessionId)
                    .set("status", AgentSession.Status.ACTIVE)
                    .set("updated_at", LocalDateTime.now());
        agentSessionMapper.update(null, updateWrapper);
    }

    @Override
    public void deleteSession(String sessionId) {
        agentSessionMapper.deleteById(sessionId);
    }

    @Override
    public void archiveOldSessions(String userId) {
        agentSessionMapper.archiveOldSessions(userId);
    }

    @Override
    public AgentCollaboration initiateCollaboration(String sessionId, String initiatorAgentId,
                                                  String targetAgentId, String taskType, String taskDescription) {
        AgentCollaboration collaboration = new AgentCollaboration();
        collaboration.setSessionId(sessionId);
        collaboration.setInitiatorAgentId(initiatorAgentId);
        collaboration.setTargetAgentId(targetAgentId);
        collaboration.setTaskType(taskType);
        collaboration.setTaskDescription(taskDescription);
        collaboration.setStatus(AgentCollaboration.Status.PENDING);
        collaboration.setCreatedAt(LocalDateTime.now());

        agentCollaborationMapper.insert(collaboration);
        return collaboration;
    }

    @Override
    public List<AgentCollaboration> getSessionCollaborations(String sessionId) {
        return agentCollaborationMapper.selectBySessionId(sessionId);
    }

    @Override
    public List<AgentCollaboration> getPendingCollaborations(String agentId) {
        return agentCollaborationMapper.selectPendingTasksByAgentId(agentId, 50);
    }

    @Override
    public AgentCollaboration completeCollaboration(String collaborationId, String result) {
        agentCollaborationMapper.completeCollaboration(collaborationId, AgentCollaboration.Status.COMPLETED, result);
        return agentCollaborationMapper.selectById(collaborationId);
    }

    @Override
    public void failCollaboration(String collaborationId, String reason) {
        agentCollaborationMapper.completeCollaboration(collaborationId, AgentCollaboration.Status.FAILED, reason);
    }

    @Override
    public Map<String, Object> getSessionStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        // 活动会话数
        int activeSessions = getActiveSessionCount(userId);
        stats.put("activeSessions", activeSessions);

        // 总会话数
        QueryWrapper<AgentSession> totalWrapper = new QueryWrapper<>();
        totalWrapper.eq("user_id", userId);
        long totalSessions = agentSessionMapper.selectCount(totalWrapper);
        stats.put("totalSessions", totalSessions);

        // 今日会话数
        QueryWrapper<AgentSession> todayWrapper = new QueryWrapper<>();
        todayWrapper.eq("user_id", userId)
                   .ge("created_at", LocalDateTime.now().toLocalDate());
        long todaySessions = agentSessionMapper.selectCount(todayWrapper);
        stats.put("todaySessions", todaySessions);

        // 各类型会话数
        Map<String, Long> sessionTypes = new HashMap<>();
        sessionTypes.put("single", getSessionCountByType(userId, AgentSession.Type.SINGLE));
        sessionTypes.put("multi", getSessionCountByType(userId, AgentSession.Type.MULTI));
        sessionTypes.put("collaboration", getSessionCountByType(userId, AgentSession.Type.COLLABORATION));
        stats.put("sessionTypes", sessionTypes);

        return stats;
    }

    @Override
    public int getActiveSessionCount(String userId) {
        return agentSessionMapper.countActiveByUserId(userId);
    }

    @Override
    public boolean canCreateSession(String userId) {
        return getActiveSessionCount(userId) < MAX_ACTIVE_SESSIONS_PER_USER;
    }

    private long getSessionCountByType(String userId, String sessionType) {
        QueryWrapper<AgentSession> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("session_type", sessionType);
        return agentSessionMapper.selectCount(wrapper);
    }
}
