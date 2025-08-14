-- 用户权限体系数据库表结构

-- 创建用户表
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    nickname VARCHAR(50),
    avatar TEXT,
    status INTEGER NOT NULL DEFAULT 1, -- 1:启用 0:禁用
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建角色表
CREATE TABLE roles (
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status INTEGER NOT NULL DEFAULT 1, -- 1:启用 0:禁用
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建权限表
CREATE TABLE permissions (
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    resource VARCHAR(100), -- 资源标识
    action VARCHAR(50), -- 操作类型 (READ, WRITE, DELETE等)
    description TEXT,
    status INTEGER NOT NULL DEFAULT 1, -- 1:启用 0:禁用
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建用户角色关联表
CREATE TABLE user_roles (
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(255) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_id)
);

-- 创建角色权限关联表
CREATE TABLE role_permissions (
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id VARCHAR(255) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id VARCHAR(255) NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE(role_id, permission_id)
);

-- 插入角色
INSERT INTO roles (name, code, description) VALUES
                                                ('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限'),
                                                ('管理员', 'ADMIN', '系统管理员'),
                                                ('用户', 'USER', '普通用户')
ON CONFLICT (code) DO NOTHING;

-- 插入权限
INSERT INTO permissions (name, code, resource, action, description) VALUES
                                                                        ('用户管理', 'USER_MANAGE', 'user', '*', '用户管理相关权限'),
                                                                        ('角色管理', 'ROLE_MANAGE', 'role', '*', '角色管理相关权限'),
                                                                        ('权限管理', 'PERMISSION_MANAGE', 'permission', '*', '权限管理相关权限'),
                                                                        ('会话管理', 'CONVERSATION_MANAGE', 'conversation', '*', '会话管理相关权限'),
                                                                        ('文档管理', 'DOCUMENT_MANAGE', 'document', '*', '文档管理相关权限')
ON CONFLICT (code) DO NOTHING;

-- 使用业务code进行角色-权限关联
-- 为超级管理员分配所有权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.code IN ('USER_MANAGE', 'ROLE_MANAGE', 'PERMISSION_MANAGE', 'CONVERSATION_MANAGE', 'DOCUMENT_MANAGE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 为管理员分配部分权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ADMIN'
  AND p.code IN ('CONVERSATION_MANAGE', 'DOCUMENT_MANAGE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 为普通用户分配基础权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'USER'
  AND p.code = 'CONVERSATION_MANAGE'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 创建默认超级管理员用户
INSERT INTO users (username, password, email, nickname, status) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyiE.5iG7bZKKCaWd2rE4ZFWsJO', 'admin@example.com', '超级管理员', 1)
ON CONFLICT (username) DO NOTHING;

-- 为admin用户分配超级管理员角色
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;
