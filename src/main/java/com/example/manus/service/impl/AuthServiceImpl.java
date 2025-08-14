package com.example.manus.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.manus.dto.LoginRequest;
import com.example.manus.dto.LoginResponse;
import com.example.manus.dto.RegisterRequest;
import com.example.manus.persistence.entity.User;
import com.example.manus.persistence.mapper.UserMapper;
import com.example.manus.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .eq(User::getStatus, 1));

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 验证密码
        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 登录
        StpUtil.login(user.getId());

        // 获取用户权限
        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        List<String> roles = userMapper.selectRolesByUserId(user.getId());

        // 返回登录信息
        return new LoginResponse(
                StpUtil.getTokenValue(),
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getAvatar(),
                roles,
                permissions
        );
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public User register(RegisterRequest request) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(request.getEmail())) {
            User existEmailUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, request.getEmail()));
            if (existEmailUser != null) {
                throw new RuntimeException("邮箱已存在");
            }
        }

        // 创建用户
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        return user;
    }

    @Override
    public User getCurrentUser() {
        String userId = StpUtil.getLoginIdAsString();
        return userMapper.selectById(userId);
    }
}
