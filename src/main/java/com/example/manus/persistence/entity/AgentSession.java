package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("agent_sessions")
public class AgentSession {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String sessionName;

    private String currentAgentId;

    private String sessionType;

    private String status;

    private String metadata;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

    public static class Type {
        public static final String SINGLE = "single";
        public static final String MULTI = "multi";
        public static final String COLLABORATION = "collaboration";
    }

    public static class Status {
        public static final String ACTIVE = "active";
        public static final String PAUSED = "paused";
        public static final String COMPLETED = "completed";
        public static final String ARCHIVED = "archived";
    }
}