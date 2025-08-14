package com.example.manus.service;

import com.example.manus.persistence.entity.AgentSession;
import com.example.manus.persistence.entity.AgentCollaboration;

import java.util.List;
import java.util.Map;

public interface AgentSessionService {

    // 会话管理
    AgentSession createSession(String userId, String sessionName, String agentId, String sessionType);
    
    AgentSession getSession(String sessionId);
    
    List<AgentSession> getActiveSessions(String userId);
    
    List<AgentSession> getUserSessions(String userId, int limit);
    
    AgentSession switchAgent(String sessionId, String agentId);
    
    AgentSession updateSession(String sessionId, String sessionName, Map<String, Object> metadata);
    
    void completeSession(String sessionId);
    
    void pauseSession(String sessionId);
    
    void resumeSession(String sessionId);
    
    void deleteSession(String sessionId);
    
    void archiveOldSessions(String userId);

    // Agent协作管理
    AgentCollaboration initiateCollaboration(String sessionId, String initiatorAgentId, 
                                           String targetAgentId, String taskType, String taskDescription);
    
    List<AgentCollaboration> getSessionCollaborations(String sessionId);
    
    List<AgentCollaboration> getPendingCollaborations(String agentId);
    
    AgentCollaboration completeCollaboration(String collaborationId, String result);
    
    void failCollaboration(String collaborationId, String reason);

    // 会话统计
    Map<String, Object> getSessionStatistics(String userId);
    
    int getActiveSessionCount(String userId);
    
    boolean canCreateSession(String userId);
}