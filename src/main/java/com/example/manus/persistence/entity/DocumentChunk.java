package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "文档分片实体")
@Data
@TableName("document_chunks")
public class DocumentChunk {

    @Schema(description = "分片ID")
    @TableId
    private String id;

    @Schema(description = "文档ID")
    private String documentId;

    @Schema(description = "知识库ID")
    private String knowledgeBaseId;

    @Schema(description = "分片内容")
    private String content;

    @Schema(description = "分片索引")
    private Integer chunkIndex;

    @Schema(description = "分片大小(字符数)")
    private Integer chunkSize;

    @Schema(description = "向量嵌入ID")
    private String embeddingId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}