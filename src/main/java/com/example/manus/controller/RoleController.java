package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.common.CommonResult;
import com.example.manus.persistence.entity.Role;
import com.example.manus.persistence.mapper.RoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

//@Tag(name = "角色管理", description = "系统角色管理相关接口")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleMapper roleMapper;

    @Operation(summary = "获取角色列表", description = "分页查询角色列表，支持按角色名、代码、状态筛选")
    @GetMapping
    @SaCheckPermission("ROLE_MANAGE")
    public CommonResult<Page<Role>> getRoles(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "角色名模糊查询") @RequestParam(required = false) String name,
            @Parameter(description = "角色代码模糊查询") @RequestParam(required = false) String code,
            @Parameter(description = "角色状态(0:禁用 1:启用)") @RequestParam(required = false) Integer status) {

        Page<Role> page = new Page<>(current, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Role::getName, name);
        }
        if (StringUtils.hasText(code)) {
            wrapper.like(Role::getCode, code);
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }

        wrapper.orderByDesc(Role::getCreatedAt);
        Page<Role> result = roleMapper.selectPage(page, wrapper);

        return CommonResult.success(result);
    }

    @Operation(summary = "获取角色详情", description = "根据角色ID获取角色详细信息")
    @GetMapping("/{id}")
    @SaCheckPermission("ROLE_MANAGE")
    public CommonResult<Role> getRoleById(@Parameter(description = "角色ID") @PathVariable String id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            return CommonResult.failed("角色不存在");
        }
        return CommonResult.success(role);
    }

    @Operation(summary = "创建角色", description = "新增系统角色")
    @PostMapping
    @SaCheckPermission("ROLE_MANAGE")
    public CommonResult<Role> createRole(@Parameter(description = "角色信息") @RequestBody Role role) {
        // 检查角色代码是否已存在
        Role existRole = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getCode, role.getCode()));
        if (existRole != null) {
            return CommonResult.failed("角色代码已存在");
        }

        role.setId(UUID.randomUUID().toString());
        role.setStatus(1);
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());

        int result = roleMapper.insert(role);
        if (result > 0) {
            return CommonResult.success(role, "创建成功");
        } else {
            return CommonResult.failed("创建失败");
        }
    }

    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping("/{id}")
    @SaCheckPermission("ROLE_MANAGE")
    public CommonResult<Void> updateRole(
            @Parameter(description = "角色ID") @PathVariable String id,
            @Parameter(description = "角色信息") @RequestBody Role role) {
        Role existRole = roleMapper.selectById(id);
        if (existRole == null) {
            return CommonResult.failed("角色不存在");
        }

        role.setId(id);
        role.setUpdatedAt(LocalDateTime.now());

        int result = roleMapper.updateById(role);
        if (result > 0) {
            return CommonResult.success(null, "更新成功");
        } else {
            return CommonResult.failed("更新失败");
        }
    }

    @Operation(summary = "删除角色", description = "删除指定角色")
    @DeleteMapping("/{id}")
    @SaCheckPermission("ROLE_MANAGE")
    public CommonResult<Void> deleteRole(@Parameter(description = "角色ID") @PathVariable String id) {
        int result = roleMapper.deleteById(id);
        if (result > 0) {
            return CommonResult.success(null, "删除成功");
        } else {
            return CommonResult.failed("删除失败");
        }
    }
}
