# 用户权限体系实现说明

本项目基于 Sa-Token + Redis 实现了完整的用户权限管理体系。

## 功能特性

### 1. 用户认证
- 用户注册/登录
- 密码BCrypt加密
- JWT Token管理
- 用户登出

### 2. 权限控制
- RBAC角色权限模型
- 基于注解的权限验证
- Redis缓存权限数据
- 细粒度权限控制

### 3. 数据模型
- **用户表(users)**: 用户基本信息
- **角色表(roles)**: 角色定义
- **权限表(permissions)**: 权限定义
- **用户角色关联表(user_roles)**: 多对多关联
- **角色权限关联表(role_permissions)**: 多对多关联

## 核心接口

### 认证接口(/auth)
```
POST /auth/login       # 用户登录
POST /auth/logout      # 用户登出
POST /auth/register    # 用户注册
GET  /auth/me          # 获取当前用户信息
```

### 用户管理(/users)
```
GET    /users          # 获取用户列表(需要USER_MANAGE权限)
GET    /users/{id}     # 获取用户详情
PUT    /users/{id}/status  # 更新用户状态
DELETE /users/{id}     # 删除用户
```

### 角色管理(/roles)
```
GET    /roles          # 获取角色列表(需要ROLE_MANAGE权限)
POST   /roles          # 创建角色
PUT    /roles/{id}     # 更新角色
DELETE /roles/{id}     # 删除角色
```

### 权限管理(/permissions)
```
GET    /permissions    # 获取权限列表(需要PERMISSION_MANAGE权限)
POST   /permissions    # 创建权限
PUT    /permissions/{id}  # 更新权限
DELETE /permissions/{id}  # 删除权限
```

## 使用说明

### 1. 数据库初始化
执行 `src/main/resources/V2.sql` 创建相关表结构和初始数据。

默认超级管理员账号:
- 用户名: admin
- 密码: admin123

### 2. 配置说明
在 `application.yml` 中配置Redis和Sa-Token相关参数。

### 3. 权限验证
在需要权限控制的接口上使用 `@SaCheckPermission` 注解:
```java
@GetMapping("/users")
@SaCheckPermission("USER_MANAGE")
public CommonResult<Page<User>> getUsers() {
    // ...
}
```

### 4. 获取当前用户
```java
String userId = StpUtil.getLoginIdAsString();
User currentUser = authService.getCurrentUser();
```

## 技术栈
- Spring Boot 3.2.5
- Sa-Token 1.38.0
- Redis
- MyBatis Plus
- PostgreSQL
- BCrypt密码加密

## 安全特性
- 密码BCrypt加密存储
- JWT Token安全管理
- 接口权限验证
- Redis缓存权限数据
- 会话超时控制