package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.example.manus.common.CommonResult;
import com.example.manus.persistence.entity.AgentDefinition;
import com.example.manus.persistence.entity.UserAgentConfig;
import com.example.manus.persistence.entity.AgentToolConfig;
import com.example.manus.service.AgentManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Agent管理", description = "AI助手管理相关接口")
@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
@SaCheckLogin
public class AgentController {

    private final AgentManagementService agentManagementService;

    @Data
    public static class UserAgentConfigRequest {
        private String agentId;
        private Boolean enabled;
        private String customName;
        private String customPrompt;
        private Boolean isFavorite;
    }

    @Data
    public static class AgentCreateRequest {
        private String code;
        private String name;
        private String displayName;
        private String description;
        private String category;
        private String avatarUrl;
        private String systemPrompt;
        private Integer maxTokens;
        private java.math.BigDecimal temperature;
        private java.math.BigDecimal topP;
    }

    @Operation(summary = "获取所有Agent", description = "获取系统中定义的所有AI助手")
    @GetMapping
    public CommonResult<List<AgentDefinition>> getAllAgents() {
        try {
            List<AgentDefinition> agents = agentManagementService.getAllAgents();
            return CommonResult.success(agents);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取启用的Agent", description = "获取系统中启用状态的AI助手")
    @GetMapping("/enabled")
    public CommonResult<List<AgentDefinition>> getEnabledAgents() {
        try {
            List<AgentDefinition> agents = agentManagementService.getEnabledAgents();
            return CommonResult.success(agents);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "按分类获取Agent", description = "根据分类获取AI助手列表")
    @GetMapping("/category/{category}")
    public CommonResult<List<AgentDefinition>> getAgentsByCategory(
            @Parameter(description = "Agent分类") @PathVariable String category) {
        try {
            List<AgentDefinition> agents = agentManagementService.getAgentsByCategory(category);
            return CommonResult.success(agents);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取Agent分类", description = "获取所有Agent分类列表")
    @GetMapping("/categories")
    public CommonResult<List<String>> getAgentCategories() {
        try {
            List<String> categories = agentManagementService.getAgentCategories();
            return CommonResult.success(categories);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取Agent详情", description = "根据ID获取Agent详细信息")
    @GetMapping("/{agentId}")
    public CommonResult<AgentDefinition> getAgentById(
            @Parameter(description = "Agent ID") @PathVariable String agentId) {
        try {
            AgentDefinition agent = agentManagementService.getAgentById(agentId);
            if (agent == null) {
                return CommonResult.failed("Agent不存在");
            }
            return CommonResult.success(agent);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "创建Agent", description = "创建自定义AI助手")
    @PostMapping
    public CommonResult<AgentDefinition> createAgent(@RequestBody AgentCreateRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            if (!agentManagementService.canCreateAgent(userId)) {
                return CommonResult.failed("没有权限创建Agent");
            }

            AgentDefinition agent = new AgentDefinition();
            agent.setCode(request.getCode());
            agent.setName(request.getName());
            agent.setDisplayName(request.getDisplayName());
            agent.setDescription(request.getDescription());
            agent.setCategory(request.getCategory());
            agent.setAvatarUrl(request.getAvatarUrl());
            agent.setSystemPrompt(request.getSystemPrompt());
            agent.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : 4000);
            agent.setTemperature(request.getTemperature() != null ? request.getTemperature() : new java.math.BigDecimal("0.7"));
            agent.setTopP(request.getTopP() != null ? request.getTopP() : new java.math.BigDecimal("0.9"));
            agent.setIsSystemAgent(false);
            agent.setCreatedBy(userId);

            AgentDefinition createdAgent = agentManagementService.createAgent(agent);
            return CommonResult.success(createdAgent, "Agent创建成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新Agent", description = "更新Agent信息")
    @PutMapping("/{agentId}")
    public CommonResult<AgentDefinition> updateAgent(
            @Parameter(description = "Agent ID") @PathVariable String agentId,
            @RequestBody AgentCreateRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            AgentDefinition existingAgent = agentManagementService.getAgentById(agentId);
            if (existingAgent == null) {
                return CommonResult.failed("Agent不存在");
            }

            // 只有创建者和管理员可以编辑
            if (!userId.equals(existingAgent.getCreatedBy()) && 
                !agentManagementService.canCreateAgent(userId)) {
                return CommonResult.failed("没有权限编辑此Agent");
            }

            existingAgent.setName(request.getName());
            existingAgent.setDisplayName(request.getDisplayName());
            existingAgent.setDescription(request.getDescription());
            existingAgent.setCategory(request.getCategory());
            existingAgent.setAvatarUrl(request.getAvatarUrl());
            existingAgent.setSystemPrompt(request.getSystemPrompt());
            if (request.getMaxTokens() != null) existingAgent.setMaxTokens(request.getMaxTokens());
            if (request.getTemperature() != null) existingAgent.setTemperature(request.getTemperature());
            if (request.getTopP() != null) existingAgent.setTopP(request.getTopP());

            AgentDefinition updatedAgent = agentManagementService.updateAgent(existingAgent);
            return CommonResult.success(updatedAgent, "Agent更新成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "删除Agent", description = "删除自定义Agent")
    @DeleteMapping("/{agentId}")
    public CommonResult<Void> deleteAgent(
            @Parameter(description = "Agent ID") @PathVariable String agentId) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            AgentDefinition agent = agentManagementService.getAgentById(agentId);
            if (agent == null) {
                return CommonResult.failed("Agent不存在");
            }

            if (agent.getIsSystemAgent()) {
                return CommonResult.failed("系统Agent不能删除");
            }

            if (!userId.equals(agent.getCreatedBy()) && !agentManagementService.canCreateAgent(userId)) {
                return CommonResult.failed("没有权限删除此Agent");
            }

            agentManagementService.deleteAgent(agentId);
            return CommonResult.success(null, "Agent删除成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取我的Agent配置", description = "获取当前用户的Agent配置")
    @GetMapping("/my-config")
    public CommonResult<List<UserAgentConfig>> getMyAgentConfigs() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<UserAgentConfig> configs = agentManagementService.getUserEnabledAgents(userId);
            return CommonResult.success(configs);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取我收藏的Agent", description = "获取当前用户收藏的Agent列表")
    @GetMapping("/my-favorites")
    public CommonResult<List<UserAgentConfig>> getMyFavoriteAgents() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<UserAgentConfig> configs = agentManagementService.getUserFavoriteAgents(userId);
            return CommonResult.success(configs);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "更新用户Agent配置", description = "更新用户对Agent的个人配置")
    @PutMapping("/my-config")
    public CommonResult<UserAgentConfig> updateMyAgentConfig(@RequestBody UserAgentConfigRequest request) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            
            if (!agentManagementService.hasAgentAccess(userId, request.getAgentId())) {
                return CommonResult.failed("没有权限访问该Agent");
            }

            UserAgentConfig config = agentManagementService.updateUserAgentConfig(
                    userId, request.getAgentId(), request.getEnabled(), 
                    request.getCustomName(), request.getCustomPrompt(), request.getIsFavorite());
            
            return CommonResult.success(config, "Agent配置更新成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "初始化用户Agent配置", description = "为当前用户初始化默认Agent配置")
    @PostMapping("/init-config")
    public CommonResult<Void> initializeAgentConfig() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            agentManagementService.initializeUserAgentConfigs(userId);
            return CommonResult.success(null, "Agent配置初始化成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取Agent工具配置", description = "获取Agent的工具配置列表")
    @GetMapping("/{agentId}/tools")
    public CommonResult<List<AgentToolConfig>> getAgentToolConfigs(
            @Parameter(description = "Agent ID") @PathVariable String agentId) {
        try {
            List<AgentToolConfig> configs = agentManagementService.getAgentToolConfigs(agentId);
            return CommonResult.success(configs);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取Agent统计信息", description = "获取用户的Agent使用统计")
    @GetMapping("/statistics")
    public CommonResult<Map<String, Object>> getAgentStatistics() {
        try {
            String userId = StpUtil.getLoginIdAsString();
            Map<String, Object> stats = agentManagementService.getAgentStatistics(userId);
            return CommonResult.success(stats);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取Agent使用统计", description = "获取用户的Agent使用统计信息")
    @GetMapping("/usage-stats")
    public CommonResult<List<Map<String, Object>>> getAgentUsageStats(
            @Parameter(description = "统计天数") @RequestParam(defaultValue = "30") int days) {
        try {
            String userId = StpUtil.getLoginIdAsString();
            List<Map<String, Object>> stats = agentManagementService.getUserAgentUsageStats(userId, days);
            return CommonResult.success(stats);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}