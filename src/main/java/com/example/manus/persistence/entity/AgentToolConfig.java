package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("agent_tool_configs")
public class AgentToolConfig {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String agentId;

    private String toolId;

    private Boolean enabled;

    private String config;

    private Integer priority;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}