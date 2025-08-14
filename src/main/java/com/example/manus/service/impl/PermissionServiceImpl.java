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


    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserPermissions(String userId) {
        return userMapper.selectPermissionsByUserId(userId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getUserRoles(String userId) {
        return userMapper.selectRolesByUserId(userId);
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
