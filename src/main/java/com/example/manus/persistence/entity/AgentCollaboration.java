package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("agent_collaborations")
public class AgentCollaboration {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String sessionId;

    private String initiatorAgentId;

    private String targetAgentId;

    private String taskType;

    private String taskDescription;

    private String status;

    private String result;

    private String metadata;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public static class TaskType {
        public static final String DELEGATE = "delegate";
        public static final String CONSULT = "consult";
        public static final String REVIEW = "review";
        public static final String ANALYZE = "analyze";
        public static final String GENERATE = "generate";
    }

    public static class Status {
        public static final String PENDING = "pending";
        public static final String IN_PROGRESS = "in_progress";
        public static final String COMPLETED = "completed";
        public static final String FAILED = "failed";
        public static final String CANCELLED = "cancelled";
    }
}