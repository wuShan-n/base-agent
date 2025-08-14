# Knife4j API文档集成说明

已成功集成Knife4j OpenAPI3，提供完整的API文档和测试界面。

## 📋 功能特性

### 1. API文档生成
- 自动生成OpenAPI 3.0规范的API文档  
- 支持中文接口描述和参数说明
- 分组管理：认证管理、用户管理、角色管理、权限管理、AI助手

### 2. 在线测试
- 直接在浏览器中测试API接口
- 支持JWT Token认证
- 参数验证和示例值
- 响应结果展示

### 3. 安全认证
- 集成Bearer Token认证
- 自动添加Authorization头
- 支持Sa-Token令牌管理

## 🚀 使用方法

### 1. 访问文档界面
启动应用后访问：`http://localhost:8080/doc.html`

### 2. API认证测试
1. 首先调用 `/auth/login` 接口登录获取token
2. 点击右上角"授权"按钮
3. 在弹窗中输入：`Bearer <你的token>`
4. 点击"授权"完成认证设置

### 3. 接口测试
- 选择要测试的接口
- 填写请求参数 
- 点击"执行"发送请求
- 查看返回结果

### 4. 默认测试账号
```
用户名: admin
密码: admin123
```

## 📚 API分组说明

### 认证管理 (/auth)
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出  
- `POST /auth/register` - 用户注册
- `GET /auth/me` - 获取当前用户信息

### 用户管理 (/users)
- `GET /users` - 获取用户列表 (需要USER_MANAGE权限)
- `GET /users/{id}` - 获取用户详情
- `PUT /users/{id}/status` - 更新用户状态
- `DELETE /users/{id}` - 删除用户

### 角色管理 (/roles)
- `GET /roles` - 获取角色列表 (需要ROLE_MANAGE权限)
- `POST /roles` - 创建角色
- `PUT /roles/{id}` - 更新角色
- `DELETE /roles/{id}` - 删除角色

### 权限管理 (/permissions)
- `GET /permissions` - 获取权限列表 (需要PERMISSION_MANAGE权限) 
- `POST /permissions` - 创建权限
- `PUT /permissions/{id}` - 更新权限
- `DELETE /permissions/{id}` - 删除权限

### AI助手 (/assistant, /documents)
- AI对话和文档管理相关接口

## ⚙️ 配置说明

### application.yml配置
```yaml
knife4j:
  enable: true
  openapi:
    title: Manus API Documentation
    description: 基于Sa-Token的用户权限管理系统API文档
    version: v1.0.0
    group:
      default:
        group-name: default
        api-rule: package
        api-rule-resources:
          - com.example.manus.controller
```

### 安全配置
- Sa-Token拦截器已配置白名单，允许访问文档相关路径
- 支持Bearer Token认证
- JWT令牌格式验证

## 📝 注解使用

### Controller层注解
```java
@Tag(name = "用户管理", description = "用户信息管理相关接口")
@Operation(summary = "获取用户列表", description = "分页查询用户列表")
@Parameter(description = "页码", example = "1")
```

### DTO层注解
```java
@Schema(description = "用户登录请求")
@Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
```

## 🛠️ 开发提示

1. **新增接口时记得添加Swagger注解**
2. **权限接口需要先登录获取token**
3. **测试完成后建议清除授权信息**
4. **生产环境建议禁用或限制访问权限**

## 📞 技术支持
如有问题请检查：
- 应用是否正常启动
- 端口是否被占用
- Redis连接是否正常
- 数据库表是否初始化完成