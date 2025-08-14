package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role_permissions")
public class RolePermission {

    @TableId
    private String id;

    private String roleId;

    private String permissionId;
}