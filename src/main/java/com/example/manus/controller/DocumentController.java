package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.common.CommonResult;
import com.example.manus.persistence.entity.KnowledgeDocument;
import com.example.manus.service.KnowledgeDocumentService;
import com.example.manus.service.KnowledgeSearchService;
import dev.langchain4j.rag.content.Content;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "文档管理", description = "知识库文档管理相关接口")
@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@SaCheckLogin
public class DocumentController {

    private final KnowledgeDocumentService knowledgeDocumentService;
    private final KnowledgeSearchService knowledgeSearchService;

    @Operation(summary = "上传文档", description = "向指定知识库上传文档")
    @PostMapping("/upload")
    public CommonResult<KnowledgeDocument> uploadDocument(
            @Parameter(description = "知识库ID") @RequestParam String knowledgeBaseId,
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file) {
        try {
            KnowledgeDocument document = knowledgeDocumentService.uploadDocument(knowledgeBaseId, file);
            return CommonResult.success(document, "文档上传成功，正在后台处理中...");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "删除文档", description = "删除指定文档")
    @DeleteMapping("/{id}")
    public CommonResult<Void> deleteDocument(@Parameter(description = "文档ID") @PathVariable String id) {
        try {
            knowledgeDocumentService.deleteDocument(id);
            return CommonResult.success(null, "文档删除成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取文档详情", description = "根据ID获取文档详细信息")
    @GetMapping("/{id}")
    public CommonResult<KnowledgeDocument> getDocumentById(@Parameter(description = "文档ID") @PathVariable String id) {
        try {
            KnowledgeDocument document = knowledgeDocumentService.getDocumentById(id);
            if (document == null) {
                return CommonResult.failed("文档不存在");
            }
            return CommonResult.success(document);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取知识库文档", description = "获取指定知识库的所有文档")
    @GetMapping("/knowledge-base/{knowledgeBaseId}")
    public CommonResult<List<KnowledgeDocument>> getDocumentsByKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String knowledgeBaseId) {
        try {
            List<KnowledgeDocument> documents = knowledgeDocumentService.getDocumentsByKnowledgeBase(knowledgeBaseId);
            return CommonResult.success(documents);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "搜索文档", description = "分页搜索文档，支持关键词搜索")
    @GetMapping("/search")
    public CommonResult<Page<KnowledgeDocument>> searchDocuments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "知识库ID") @RequestParam(required = false) String knowledgeBaseId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        try {
            Page<KnowledgeDocument> result = knowledgeDocumentService.searchDocuments(current, size, knowledgeBaseId, keyword);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "重新处理文档", description = "重新处理文档的向量化")
    @PostMapping("/{id}/reprocess")
    public CommonResult<Void> reprocessDocument(@Parameter(description = "文档ID") @PathVariable String id) {
        try {
            // 检查权限
            KnowledgeDocument document = knowledgeDocumentService.getDocumentById(id);
            if (document == null) {
                return CommonResult.failed("文档不存在");
            }

            knowledgeDocumentService.processDocument(id);
            return CommonResult.success(null, "文档重新处理中...");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "搜索知识库内容", description = "在指定知识库中搜索相关内容")
    @GetMapping("/knowledge-base/{knowledgeBaseId}/search")
    public CommonResult<List<Content>> searchKnowledgeBaseContent(
            @Parameter(description = "知识库ID") @PathVariable String knowledgeBaseId,
            @Parameter(description = "搜索查询") @RequestParam String query,
            @Parameter(description = "最大结果数") @RequestParam(defaultValue = "5") int maxResults) {
        try {
            List<Content> results = knowledgeSearchService.searchByKnowledgeBase(knowledgeBaseId, query, maxResults);
            return CommonResult.success(results);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}
