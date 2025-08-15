package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.common.CommonResult;
import com.example.manus.persistence.entity.KnowledgeBase;
import com.example.manus.service.KnowledgeBaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@Tag(name = "知识库管理", description = "用户知识库管理相关接口")
@RestController
@RequestMapping("/knowledge-bases")
@RequiredArgsConstructor
@SaCheckLogin
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @Operation(summary = "创建知识库", description = "用户创建新的知识库")
    @PostMapping
    public CommonResult<KnowledgeBase> createKnowledgeBase(@RequestBody KnowledgeBase knowledgeBase) {
        try {
            KnowledgeBase created = knowledgeBaseService.createKnowledgeBase(knowledgeBase);
            return CommonResult.success(created, "知识库创建成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新知识库", description = "更新知识库信息")
    @PutMapping("/{id}")
    public CommonResult<KnowledgeBase> updateKnowledgeBase(
            @Parameter(description = "知识库ID") @PathVariable String id,
            @RequestBody KnowledgeBase knowledgeBase) {
        try {
            knowledgeBase.setId(id);
            KnowledgeBase updated = knowledgeBaseService.updateKnowledgeBase(knowledgeBase);
            return CommonResult.success(updated, "知识库更新成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "删除知识库", description = "删除指定知识库")
    @DeleteMapping("/{id}")
    public CommonResult<Void> deleteKnowledgeBase(@Parameter(description = "知识库ID") @PathVariable String id) {
        try {
            knowledgeBaseService.deleteKnowledgeBase(id);
            return CommonResult.success(null, "知识库删除成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取知识库详情", description = "根据ID获取知识库详细信息")
    @GetMapping("/{id}")
    public CommonResult<KnowledgeBase> getKnowledgeBaseById(@Parameter(description = "知识库ID") @PathVariable String id) {
        try {
            KnowledgeBase knowledgeBase = knowledgeBaseService.getKnowledgeBaseById(id);
            if (knowledgeBase == null) {
                return CommonResult.failed("知识库不存在");
            }
            return CommonResult.success(knowledgeBase);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取我的知识库", description = "获取当前用户的所有知识库")
    @GetMapping("/my")
    public CommonResult<List<KnowledgeBase>> getMyKnowledgeBases() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<KnowledgeBase> knowledgeBases = knowledgeBaseService.getUserKnowledgeBases(userId);
            return CommonResult.success(knowledgeBases);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取公开知识库", description = "获取所有公开的知识库")
    @GetMapping("/public")
    public CommonResult<List<KnowledgeBase>> getPublicKnowledgeBases() {
        try {
            List<KnowledgeBase> knowledgeBases = knowledgeBaseService.getPublicKnowledgeBases();
            return CommonResult.success(knowledgeBases);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "搜索知识库", description = "分页搜索知识库，支持关键词搜索")
    @GetMapping("/search")
    public CommonResult<Page<KnowledgeBase>> searchKnowledgeBases(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) String userId,
            @Parameter(description = "是否公开(0:私有 1:公开)") @RequestParam(required = false) Integer isPublic) {
        try {
            Page<KnowledgeBase> result = knowledgeBaseService.searchKnowledgeBases(current, size, keyword, userId, isPublic);
            return CommonResult.success(result);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新知识库统计", description = "手动更新知识库的统计信息")
    @PostMapping("/{id}/update-stats")
    public CommonResult<Void> updateKnowledgeBaseStats(@Parameter(description = "知识库ID") @PathVariable String id) {
        try {
            // 检查权限
            if (!knowledgeBaseService.hasAccessPermission(id, StpUtil.getLoginIdAsString())) {
                return CommonResult.failed("无权限操作此知识库");
            }
            knowledgeBaseService.updateKnowledgeBaseStats(id);
            return CommonResult.success(null, "统计信息更新成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}
