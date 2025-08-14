package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("agent_definitions")
public class AgentDefinition {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String code;

    private String name;

    private String displayName;

    private String description;

    private String category;

    private String avatarUrl;

    private String systemPrompt;

    private Integer maxTokens;

    private BigDecimal temperature;

    private BigDecimal topP;

    private Integer status;

    private Boolean isSystemAgent;

    private String createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static class Category {
        public static final String GENERAL = "general";
        public static final String DEVELOPMENT = "development";
        public static final String CREATIVE = "creative";
        public static final String ANALYSIS = "analysis";
        public static final String RESEARCH = "research";
        public static final String SUPPORT = "support";
    }

    public static class Status {
        public static final int ENABLED = 1;
        public static final int DISABLED = 0;
    }
}