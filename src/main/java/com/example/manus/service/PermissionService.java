package com.example.manus.service;

import java.util.List;

public interface PermissionService {

    List<String> getUserPermissions(String userId);

    List<String> getUserRoles(String userId);

    boolean hasPermission(String userId, String permission);

    boolean hasRole(String userId, String role);

    boolean hasAnyPermission(String userId, String... permissions);

    boolean hasAnyRole(String userId, String... roles);
}