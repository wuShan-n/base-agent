-- 工具管理系统数据库表结构

-- 创建工具定义表
CREATE TABLE tool_definitions (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
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
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
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
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
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


-- 初始化基础工具定义数据
INSERT INTO tool_definitions ( name, code, display_name, description, category, icon, bean_class, default_enabled, require_permission, permission_code, sort_order) VALUES
( 'calculatorTool', 'CALCULATOR', '计算器', '进行数学四则运算和复杂数学计算', '实用工具', 'calculator', 'com.example.manus.tool.CalculatorTool', true, false, null, 1),
( 'knowledgeBaseTool', 'KNOWLEDGE_BASE', '知识库搜索', '从知识库中检索相关内容回答问题', '知识管理', 'database', 'com.example.manus.tool.KnowledgeBaseTool', true, false, null, 2),
('personalKnowledgeBaseTool', 'PERSONAL_KNOWLEDGE', '个人知识库', '搜索用户个人知识库内容', '知识管理', 'user-database', 'com.example.manus.tool.PersonalKnowledgeBaseTool', true, false, null, 3),
( 'webScraperTool', 'WEB_SCRAPER', '网页抓取', '获取指定URL网页的文本内容', '网络工具', 'globe', 'com.example.manus.tool.WebScraperTool', false, false, null, 4),
( 'terminalTool', 'TERMINAL', '终端命令', '执行系统终端命令', '系统工具', 'terminal', 'com.example.manus.tool.TerminalTool', false, true, 'SYSTEM_ADMIN', 5);

