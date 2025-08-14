package com.example.manus.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.common.CommonResult;
import com.example.manus.persistence.entity.User;
import com.example.manus.persistence.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户信息管理相关接口")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;

    @Operation(summary = "获取用户列表", description = "分页查询用户列表，支持按用户名、昵称、状态筛选")
    @GetMapping
    @SaCheckPermission("USER_MANAGE")
    public CommonResult<Page<User>> getUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "用户名模糊查询") @RequestParam(required = false) String username,
            @Parameter(description = "昵称模糊查询") @RequestParam(required = false) String nickname,
            @Parameter(description = "用户状态(0:禁用 1:启用)") @RequestParam(required = false) Integer status) {
        
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(username)) {
            wrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(nickname)) {
            wrapper.like(User::getNickname, nickname);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(page, wrapper);
        
        // 不返回密码
        result.getRecords().forEach(user -> user.setPassword(null));
        
        return CommonResult.success(result);
    }

    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    @SaCheckPermission("USER_MANAGE")
    public CommonResult<User> getUserById(@Parameter(description = "用户ID") @PathVariable String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return CommonResult.failed("用户不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return CommonResult.success(user);
    }

    @Operation(summary = "更新用户状态", description = "启用或禁用用户账号")
    @PutMapping("/{id}/status")
    @SaCheckPermission("USER_MANAGE")
    public CommonResult<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable String id, 
            @Parameter(description = "用户状态(0:禁用 1:启用)") @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        
        int result = userMapper.updateById(user);
        if (result > 0) {
            return CommonResult.success(null, "更新成功");
        } else {
            return CommonResult.failed("更新失败");
        }
    }

    @Operation(summary = "删除用户", description = "删除指定用户")
    @DeleteMapping("/{id}")
    @SaCheckPermission("USER_MANAGE")
    public CommonResult<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable String id) {
        int result = userMapper.deleteById(id);
        if (result > 0) {
            return CommonResult.success(null, "删除成功");
        } else {
            return CommonResult.failed("删除失败");
        }
    }
}