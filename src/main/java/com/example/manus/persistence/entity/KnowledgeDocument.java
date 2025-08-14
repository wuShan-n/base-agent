package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "知识库文档实体")
@Data
@TableName("knowledge_documents")
public class KnowledgeDocument {

    @Schema(description = "文档ID")
    @TableId
    private String id;

    @Schema(description = "知识库ID")
    private String knowledgeBaseId;

    @Schema(description = "文档名称")
    private String name;

    @Schema(description = "原始文件名")
    private String originalName;

    @Schema(description = "文件类型")
    private String fileType;

    @Schema(description = "文件大小(字节)")
    private Long fileSize;

    @Schema(description = "文件路径")
    private String filePath;

    @Schema(description = "文档内容摘要")
    private String summary;

    @Schema(description = "处理状态(0:待处理 1:处理中 2:已完成 3:失败)")
    private Integer processStatus;

    @Schema(description = "分片数量")
    private Integer chunkCount;

    @Schema(description = "上传用户ID")
    private String userId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}