package com.example.manus.service.impl;

import com.example.manus.persistence.mapper.UserMapper;
import com.example.manus.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_PERMISSIONS_KEY = "user:permissions:";
    private static final String USER_ROLES_KEY = "user:roles:";
    private static final long CACHE_EXPIRE_SECONDS = 3600; // 1小时缓存

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(String userId) {
        String key = USER_PERMISSIONS_KEY + userId;
        List<String> permissions = (List<String>) redisTemplate.opsForValue().get(key);
        
        if (permissions == null) {
            permissions = userMapper.selectPermissionsByUserId(userId);
            redisTemplate.opsForValue().set(key, permissions, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
        
        return permissions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(String userId) {
        String key = USER_ROLES_KEY + userId;
        List<String> roles = (List<String>) redisTemplate.opsForValue().get(key);
        
        if (roles == null) {
            roles = userMapper.selectRolesByUserId(userId);
            redisTemplate.opsForValue().set(key, roles, CACHE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        }
        
        return roles;
    }

    @Override
    public boolean hasPermission(String userId, String permission) {
        List<String> permissions = getUserPermissions(userId);
        return permissions != null && permissions.contains(permission);
    }

    @Override
    public boolean hasRole(String userId, String role) {
        List<String> roles = getUserRoles(userId);
        return roles != null && roles.contains(role);
    }

    @Override
    public boolean hasAnyPermission(String userId, String... permissions) {
        List<String> userPermissions = getUserPermissions(userId);
        if (userPermissions == null) {
            return false;
        }
        
        for (String permission : permissions) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAnyRole(String userId, String... roles) {
        List<String> userRoles = getUserRoles(userId);
        if (userRoles == null) {
            return false;
        }
        
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }
}