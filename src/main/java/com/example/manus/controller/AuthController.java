package com.example.manus.controller;

import com.example.manus.common.CommonResult;
import com.example.manus.dto.LoginRequest;
import com.example.manus.dto.LoginResponse;
import com.example.manus.dto.RegisterRequest;
import com.example.manus.persistence.entity.User;
import com.example.manus.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录、注册、登出等认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "用户通过用户名和密码进行登录认证")
    @PostMapping("/login")
    public CommonResult<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return CommonResult.success(response);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "用户登出", description = "用户登出，清除登录状态")
    @PostMapping("/logout")
    public CommonResult<Void> logout() {
        try {
            authService.logout();
            return CommonResult.success(null, "登出成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "用户注册", description = "新用户注册账号")
    @PostMapping("/register")
    public CommonResult<User> register(@RequestBody @Validated RegisterRequest request) {
        try {
            User user = authService.register(request);
            // 不返回密码
            user.setPassword(null);
            return CommonResult.success(user, "注册成功");
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public CommonResult<User> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            // 不返回密码
            user.setPassword(null);
            return CommonResult.success(user);
        } catch (Exception e) {
            return CommonResult.failed(e.getMessage());
        }
    }
}