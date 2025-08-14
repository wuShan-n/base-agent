package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "知识库实体")
@Data
@TableName("knowledge_bases")
public class KnowledgeBase {

    @Schema(description = "知识库ID")
    @TableId
    private String id;

    @Schema(description = "知识库名称")
    private String name;

    @Schema(description = "知识库描述")
    private String description;

    @Schema(description = "拥有者用户ID")
    private String userId;

    @Schema(description = "是否公开(0:私有 1:公开)")
    private Integer isPublic;

    @Schema(description = "状态(0:禁用 1:启用)")
    private Integer status;

    @Schema(description = "文档数量")
    private Integer documentCount;

    @Schema(description = "总大小(字节)")
    private Long totalSize;

    @Schema(description = "最后更新时间")
    private LocalDateTime lastUpdated;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")  
    private LocalDateTime updatedAt;
}