package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_agent_configs")
public class UserAgentConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String agentId;

    private Boolean enabled;

    private String customName;

    private String customPrompt;

    private String config;

    private Boolean isFavorite;

    private Integer usageCount;

    private LocalDateTime lastUsedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}