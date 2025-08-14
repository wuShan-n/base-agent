package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户工具配置实体")
@Data
@TableName("user_tool_configs")
public class UserToolConfig {

    @Schema(description = "配置ID")
    @TableId
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "工具ID")
    private String toolId;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "工具配置参数(JSON)")
    private String config;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}