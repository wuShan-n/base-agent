package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.manus.common.CommonResult;
import com.example.manus.persistence.entity.ToolDefinition;
import com.example.manus.persistence.entity.UserToolConfig;
import com.example.manus.service.DynamicToolLoadingService;
import com.example.manus.service.ToolManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "工具管理", description = "AI工具管理相关接口")
@RestController
@RequestMapping("/tools")
@RequiredArgsConstructor
@SaCheckLogin
public class ToolManagementController {

    private final ToolManagementService toolManagementService;
    private final DynamicToolLoadingService dynamicToolLoadingService;

    @Data
    public static class UserToolConfigRequest {
        private String toolId;
        private Boolean enabled;
        private String config;
    }

    @Operation(summary = "获取所有工具", description = "获取系统中定义的所有工具")
    @GetMapping
    public CommonResult<List<ToolDefinition>> getAllTools() {
        try {
            List<ToolDefinition> tools = toolManagementService.getAllTools();
            return CommonResult.success(tools);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取启用的工具", description = "获取系统中启用状态的工具")
    @GetMapping("/enabled")
    public CommonResult<List<ToolDefinition>> getEnabledTools() {
        try {
            List<ToolDefinition> tools = toolManagementService.getEnabledTools();
            return CommonResult.success(tools);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "按分类获取工具", description = "根据分类获取工具列表")
    @GetMapping("/category/{category}")
    public CommonResult<List<ToolDefinition>> getToolsByCategory(
            @Parameter(description = "工具分类") @PathVariable String category) {
        try {
            List<ToolDefinition> tools = toolManagementService.getToolsByCategory(category);
            return CommonResult.success(tools);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取工具分类", description = "获取所有工具分类列表")
    @GetMapping("/categories")
    public CommonResult<List<String>> getToolCategories() {
        try {
            List<String> categories = toolManagementService.getToolCategories();
            return CommonResult.success(categories);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取我的工具配置", description = "获取当前用户的工具配置")
    @GetMapping("/my-config")
    public CommonResult<List<UserToolConfig>> getMyToolConfigs() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<UserToolConfig> configs = toolManagementService.getUserToolConfigs(userId);
            return CommonResult.success(configs);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取我启用的工具", description = "获取当前用户启用的工具列表")
    @GetMapping("/my-enabled")
    public CommonResult<List<UserToolConfig>> getMyEnabledTools() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<UserToolConfig> configs = toolManagementService.getUserEnabledTools(userId);
            return CommonResult.success(configs);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新工具配置", description = "更新用户的工具启用状态和配置")
    @PutMapping("/config")
    public CommonResult<UserToolConfig> updateToolConfig(@RequestBody UserToolConfigRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            // 检查工具权限
            ToolDefinition tool = toolManagementService.getAllTools().stream()
                    .filter(t -> t.getId().equals(request.getToolId()))
                    .findFirst()
                    .orElse(null);
                    
            if (tool == null) {
                return CommonResult.failed("工具不存在");
            }
            
            if (!toolManagementService.hasToolPermission(userId, tool.getCode())) {
                return CommonResult.failed("没有权限使用此工具");
            }
            
            UserToolConfig config = toolManagementService.updateUserToolConfig(
                    userId, request.getToolId(), request.getEnabled(), request.getConfig());
            return CommonResult.success(config, "工具配置更新成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "批量更新工具配置", description = "批量更新用户的工具配置")
    @PutMapping("/config/batch")
    public CommonResult<Void> batchUpdateToolConfig(@RequestBody List<UserToolConfigRequest> requests) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            for (UserToolConfigRequest request : requests) {
                ToolDefinition tool = toolManagementService.getAllTools().stream()
                        .filter(t -> t.getId().equals(request.getToolId()))
                        .findFirst()
                        .orElse(null);
                        
                if (tool != null && toolManagementService.hasToolPermission(userId, tool.getCode())) {
                    toolManagementService.updateUserToolConfig(
                            userId, request.getToolId(), request.getEnabled(), request.getConfig());
                }
            }
            
            return CommonResult.success(null, "批量更新成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取工具使用统计", description = "获取用户的工具使用统计信息")
    @GetMapping("/usage-stats")
    public CommonResult<List<Map<String, Object>>> getToolUsageStats(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "30") int days) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<Map<String, Object>> stats = toolManagementService.getUserToolUsageStats(userId, days);
            return CommonResult.success(stats);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "检查工具可用性", description = "检查指定工具是否可用")
    @GetMapping("/check/{toolCode}")
    public CommonResult<Boolean> checkToolAvailability(
            @Parameter(description = "工具代码") @PathVariable String toolCode) {
        try {
            boolean available = dynamicToolLoadingService.isToolAvailable(toolCode);
            return CommonResult.success(available);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取可用工具代码", description = "获取所有可用的工具代码列表")
    @GetMapping("/available-codes")
    public CommonResult<List<String>> getAvailableToolCodes() {
        try {
            List<String> codes = dynamicToolLoadingService.getAvailableToolCodes();
            return CommonResult.success(codes);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "初始化工具配置", description = "为当前用户初始化默认工具配置")
    @PostMapping("/init-config")
    public CommonResult<Void> initializeToolConfig() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            toolManagementService.initializeUserToolConfigs(userId);
            return CommonResult.success(null, "工具配置初始化成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}