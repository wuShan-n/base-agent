package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "工具定义实体")
@Data
@TableName("tool_definitions")
public class ToolDefinition {

    @Schema(description = "工具ID")
    @TableId
    private String id;

    @Schema(description = "工具名称")
    private String name;

    @Schema(description = "工具代码标识")
    private String code;

    @Schema(description = "工具显示名称")
    private String displayName;

    @Schema(description = "工具描述")
    private String description;

    @Schema(description = "工具类别")
    private String category;

    @Schema(description = "工具图标")
    private String icon;

    @Schema(description = "Bean类名")
    private String beanClass;

    @Schema(description = "是否默认启用")
    private Boolean defaultEnabled;

    @Schema(description = "是否需要权限")
    private Boolean requirePermission;

    @Schema(description = "所需权限代码")
    private String permissionCode;

    @Schema(description = "工具状态(0:禁用 1:启用)")
    private Integer status;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}