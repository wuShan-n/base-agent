package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.common.CommonResult;
import com.example.manus.persistence.entity.Permission;
import com.example.manus.persistence.mapper.PermissionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

//@Tag(name = "权限管理", description = "系统权限管理相关接口")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionMapper permissionMapper;

    @Operation(summary = "获取权限列表", description = "分页查询权限列表，支持按名称、代码、资源、状态筛选")
    @GetMapping
    @SaCheckPermission("PERMISSION_MANAGE")
    public CommonResult<Page<Permission>> getPermissions(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "权限名称模糊查询") @RequestParam(required = false) String name,
            @Parameter(description = "权限代码模糊查询") @RequestParam(required = false) String code,
            @Parameter(description = "资源模糊查询") @RequestParam(required = false) String resource,
            @Parameter(description = "权限状态(0:禁用 1:启用)") @RequestParam(required = false) Integer status) {

        Page<Permission> page = new Page<>(current, size);
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Permission::getName, name);
        }
        if (StringUtils.hasText(code)) {
            wrapper.like(Permission::getCode, code);
        }
        if (StringUtils.hasText(resource)) {
            wrapper.like(Permission::getResource, resource);
        }
        if (status != null) {
            wrapper.eq(Permission::getStatus, status);
        }

        wrapper.orderByDesc(Permission::getCreatedAt);
        Page<Permission> result = permissionMapper.selectPage(page, wrapper);

        return CommonResult.success(result);
    }

    @Operation(summary = "获取权限详情", description = "根据权限ID获取权限详细信息")
    @GetMapping("/{id}")
    @SaCheckPermission("PERMISSION_MANAGE")
    public CommonResult<Permission> getPermissionById(@Parameter(description = "权限ID") @PathVariable String id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            return CommonResult.failed("权限不存在");
        }
        return CommonResult.success(permission);
    }

    @Operation(summary = "创建权限", description = "新增系统权限")
    @PostMapping
    @SaCheckPermission("PERMISSION_MANAGE")
    public CommonResult<Permission> createPermission(@Parameter(description = "权限信息") @RequestBody Permission permission) {
        // 检查权限代码是否已存在
        Permission existPermission = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getCode, permission.getCode()));
        if (existPermission != null) {
            return CommonResult.failed("权限代码已存在");
        }

        permission.setId(UUID.randomUUID().toString());
        permission.setStatus(1);
        permission.setCreatedAt(LocalDateTime.now());
        permission.setUpdatedAt(LocalDateTime.now());

        int result = permissionMapper.insert(permission);
        if (result > 0) {
            return CommonResult.success(permission, "创建成功");
        } else {
            return CommonResult.failed("创建失败");
        }
    }

    @Operation(summary = "更新权限", description = "更新权限信息")
    @PutMapping("/{id}")
    @SaCheckPermission("PERMISSION_MANAGE")
    public CommonResult<Void> updatePermission(
            @Parameter(description = "权限ID") @PathVariable String id,
            @Parameter(description = "权限信息") @RequestBody Permission permission) {
        Permission existPermission = permissionMapper.selectById(id);
        if (existPermission == null) {
            return CommonResult.failed("权限不存在");
        }

        permission.setId(id);
        permission.setUpdatedAt(LocalDateTime.now());

        int result = permissionMapper.updateById(permission);
        if (result > 0) {
            return CommonResult.success(null, "更新成功");
        } else {
            return CommonResult.failed("更新失败");
        }
    }

    @Operation(summary = "删除权限", description = "删除指定权限")
    @DeleteMapping("/{id}")
    @SaCheckPermission("PERMISSION_MANAGE")
    public CommonResult<Void> deletePermission(@Parameter(description = "权限ID") @PathVariable String id) {
        int result = permissionMapper.deleteById(id);
        if (result > 0) {
            return CommonResult.success(null, "删除成功");
        } else {
            return CommonResult.failed("删除失败");
        }
    }
}
