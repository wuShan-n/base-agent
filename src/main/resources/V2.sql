-- 用户权限体系数据库表结构
-- 清理旧表
DROP TABLE IF EXISTS user_roles, role_permissions, users, roles, permissions CASCADE;

-- 创建用户表
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,
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
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    status INTEGER NOT NULL DEFAULT 1, -- 1:启用 0:禁用
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建权限表
CREATE TABLE permissions (
    id VARCHAR(255) PRIMARY KEY,
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
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id VARCHAR(255) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    UNIQUE(user_id, role_id)
);

-- 创建角色权限关联表
CREATE TABLE role_permissions (
    id VARCHAR(255) PRIMARY KEY,
    role_id VARCHAR(255) NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id VARCHAR(255) NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE(role_id, permission_id)
);

-- 创建索引
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- 更新用户表的触发器
CREATE OR REPLACE FUNCTION update_users_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
EXECUTE FUNCTION update_users_updated_at();

-- 更新角色表的触发器
CREATE OR REPLACE FUNCTION update_roles_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW
EXECUTE FUNCTION update_roles_updated_at();

-- 更新权限表的触发器
CREATE OR REPLACE FUNCTION update_permissions_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_permissions_updated_at
    BEFORE UPDATE ON permissions
    FOR EACH ROW
EXECUTE FUNCTION update_permissions_updated_at();

-- 初始化基础数据
INSERT INTO roles (id, name, code, description) VALUES
('1', '超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限'),
('2', '管理员', 'ADMIN', '系统管理员'),
('3', '用户', 'USER', '普通用户');

INSERT INTO permissions (id, name, code, resource, action, description) VALUES
('1', '用户管理', 'USER_MANAGE', 'user', '*', '用户管理相关权限'),
('2', '角色管理', 'ROLE_MANAGE', 'role', '*', '角色管理相关权限'),
('3', '权限管理', 'PERMISSION_MANAGE', 'permission', '*', '权限管理相关权限'),
('4', '会话管理', 'CONVERSATION_MANAGE', 'conversation', '*', '会话管理相关权限'),
('5', '文档管理', 'DOCUMENT_MANAGE', 'document', '*', '文档管理相关权限');

-- 超级管理员拥有所有权限
INSERT INTO role_permissions (id, role_id, permission_id) VALUES
('1', '1', '1'),
('2', '1', '2'),
('3', '1', '3'),
('4', '1', '4'),
('5', '1', '5');

-- 管理员拥有部分权限
INSERT INTO role_permissions (id, role_id, permission_id) VALUES
('6', '2', '4'),
('7', '2', '5');

-- 普通用户基础权限
INSERT INTO role_permissions (id, role_id, permission_id) VALUES
('8', '3', '4');

-- 创建默认超级管理员用户 (密码: admin123)
INSERT INTO users (id, username, password, email, nickname, status) VALUES
('1', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKyiE.5iG7bZKKCaWd2rE4ZFWsJO', 'admin@example.com', '超级管理员', 1);

-- 分配超级管理员角色
INSERT INTO user_roles (id, user_id, role_id) VALUES
('1', '1', '1');