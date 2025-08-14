package com.example.manus.config;

import cn.dev33.satoken.stp.StpInterface;
import com.example.manus.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionService permissionService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return permissionService.getUserPermissions(String.valueOf(loginId));
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return permissionService.getUserRoles(String.valueOf(loginId));
    }
}
