package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "工具使用记录实体")
@Data
@TableName("tool_usage_logs")
public class ToolUsageLog {

    @Schema(description = "日志ID")
    @TableId
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "工具ID")
    private String toolId;

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "工具输入参数")
    private String inputParams;

    @Schema(description = "工具执行结果")
    private String result;

    @Schema(description = "执行状态(0:失败 1:成功)")
    private Integer status;

    @Schema(description = "执行耗时(毫秒)")
    private Long executionTime;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}