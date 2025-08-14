-- 工具管理系统数据库表结构
-- 清理旧表
DROP TABLE IF EXISTS tool_usage_logs, user_tool_configs, tool_definitions CASCADE;

-- 创建工具定义表
CREATE TABLE tool_definitions (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    icon VARCHAR(100),
    bean_class VARCHAR(255) NOT NULL,
    default_enabled BOOLEAN NOT NULL DEFAULT false,
    require_permission BOOLEAN NOT NULL DEFAULT false,
    permission_code VARCHAR(50),
    status INTEGER NOT NULL DEFAULT 1, -- 0:禁用 1:启用
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建用户工具配置表
CREATE TABLE user_tool_configs (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tool_id VARCHAR(255) NOT NULL REFERENCES tool_definitions(id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT true,
    config TEXT, -- JSON配置参数
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE(user_id, tool_id)
);

-- 创建工具使用记录表
CREATE TABLE tool_usage_logs (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tool_id VARCHAR(255) NOT NULL REFERENCES tool_definitions(id) ON DELETE CASCADE,
    conversation_id VARCHAR(255) REFERENCES conversations(id) ON DELETE SET NULL,
    input_params TEXT,
    result TEXT,
    status INTEGER NOT NULL DEFAULT 1, -- 0:失败 1:成功
    execution_time BIGINT, -- 执行耗时(毫秒)
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建索引
CREATE INDEX idx_tool_definitions_status ON tool_definitions(status);
CREATE INDEX idx_tool_definitions_category ON tool_definitions(category);
CREATE INDEX idx_tool_definitions_code ON tool_definitions(code);
CREATE INDEX idx_tool_definitions_sort ON tool_definitions(sort_order);

CREATE INDEX idx_user_tool_configs_user_id ON user_tool_configs(user_id);
CREATE INDEX idx_user_tool_configs_tool_id ON user_tool_configs(tool_id);
CREATE INDEX idx_user_tool_configs_enabled ON user_tool_configs(enabled);

CREATE INDEX idx_tool_usage_logs_user_id ON tool_usage_logs(user_id);
CREATE INDEX idx_tool_usage_logs_tool_id ON tool_usage_logs(tool_id);
CREATE INDEX idx_tool_usage_logs_created_at ON tool_usage_logs(created_at);
CREATE INDEX idx_tool_usage_logs_conversation_id ON tool_usage_logs(conversation_id);

-- 创建工具定义更新时间触发器
CREATE OR REPLACE FUNCTION update_tool_definitions_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_tool_definitions_updated_at
    BEFORE UPDATE ON tool_definitions
    FOR EACH ROW
EXECUTE FUNCTION update_tool_definitions_updated_at();

-- 创建用户工具配置更新时间触发器
CREATE OR REPLACE FUNCTION update_user_tool_configs_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_user_tool_configs_updated_at
    BEFORE UPDATE ON user_tool_configs
    FOR EACH ROW
EXECUTE FUNCTION update_user_tool_configs_updated_at();

-- 初始化基础工具定义数据
INSERT INTO tool_definitions (id, name, code, display_name, description, category, icon, bean_class, default_enabled, require_permission, permission_code, sort_order) VALUES
('1', 'calculatorTool', 'CALCULATOR', '计算器', '进行数学四则运算和复杂数学计算', '实用工具', 'calculator', 'com.example.manus.tool.CalculatorTool', true, false, null, 1),
('2', 'knowledgeBaseTool', 'KNOWLEDGE_BASE', '知识库搜索', '从知识库中检索相关内容回答问题', '知识管理', 'database', 'com.example.manus.tool.KnowledgeBaseTool', true, false, null, 2),
('3', 'personalKnowledgeBaseTool', 'PERSONAL_KNOWLEDGE', '个人知识库', '搜索用户个人知识库内容', '知识管理', 'user-database', 'com.example.manus.tool.PersonalKnowledgeBaseTool', true, false, null, 3),
('4', 'webScraperTool', 'WEB_SCRAPER', '网页抓取', '获取指定URL网页的文本内容', '网络工具', 'globe', 'com.example.manus.tool.WebScraperTool', false, false, null, 4),
('5', 'terminalTool', 'TERMINAL', '终端命令', '执行系统终端命令', '系统工具', 'terminal', 'com.example.manus.tool.TerminalTool', false, true, 'SYSTEM_ADMIN', 5);

-- 初始化工具权限
INSERT INTO permissions (id, name, code, resource, action, description) VALUES
('8', '系统管理', 'SYSTEM_ADMIN', 'system', '*', '系统管理相关权限')
ON CONFLICT (id) DO NOTHING;

-- 给超级管理员添加系统管理权限
INSERT INTO role_permissions (id, role_id, permission_id) VALUES
('12', '1', '8')
ON CONFLICT (id) DO NOTHING;

-- 为所有现有用户初始化默认工具配置
INSERT INTO user_tool_configs (id, user_id, tool_id, enabled)
SELECT 
    CONCAT(u.id, '-', td.id) as id,
    u.id as user_id,
    td.id as tool_id,
    td.default_enabled as enabled
FROM users u
CROSS JOIN tool_definitions td
WHERE td.status = 1
ON CONFLICT (user_id, tool_id) DO NOTHING;