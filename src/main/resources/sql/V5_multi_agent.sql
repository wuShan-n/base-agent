-- Multi-Agent System Database Schema
-- Created for Manus AI Assistant Platform
-- Version: 5.0

-- Agent定义表 - 存储不同类型的AI助手定义
CREATE TABLE agent_definitions (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL DEFAULT 'general',
    avatar_url VARCHAR(500),
    system_prompt TEXT NOT NULL,
    max_tokens INTEGER DEFAULT 4000,
    temperature DECIMAL(3,2) DEFAULT 0.7,
    top_p DECIMAL(3,2) DEFAULT 0.9,
    status INTEGER NOT NULL DEFAULT 1, -- 1:启用 0:禁用
    is_system_agent BOOLEAN DEFAULT FALSE, -- 是否为系统内置Agent
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 添加索引
CREATE INDEX idx_agent_definitions_code ON agent_definitions(code);
CREATE INDEX idx_agent_definitions_category ON agent_definitions(category);
CREATE INDEX idx_agent_definitions_status ON agent_definitions(status);

-- Agent工具配置表 - Agent可用的工具配置
CREATE TABLE agent_tool_configs (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(255) NOT NULL,
    tool_id VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    config JSONB,
    priority INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (agent_id) REFERENCES agent_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (tool_id) REFERENCES tool_definitions(id) ON DELETE CASCADE,
    UNIQUE(agent_id, tool_id)
);

-- 添加索引
CREATE INDEX idx_agent_tool_configs_agent_id ON agent_tool_configs(agent_id);
CREATE INDEX idx_agent_tool_configs_enabled ON agent_tool_configs(enabled);

-- Agent知识库权限表 - Agent可访问的知识库
CREATE TABLE agent_knowledge_permissions (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    agent_id VARCHAR(255) NOT NULL,
    knowledge_base_id VARCHAR(255) NOT NULL,
    access_type VARCHAR(20) NOT NULL DEFAULT 'read', -- read, write, admin
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (agent_id) REFERENCES agent_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (knowledge_base_id) REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    UNIQUE(agent_id, knowledge_base_id)
);

-- 添加索引
CREATE INDEX idx_agent_knowledge_permissions_agent_id ON agent_knowledge_permissions(agent_id);
CREATE INDEX idx_agent_knowledge_permissions_kb_id ON agent_knowledge_permissions(knowledge_base_id);

-- 用户Agent配置表 - 用户对Agent的个人配置
CREATE TABLE user_agent_configs (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    custom_name VARCHAR(100), -- 用户自定义Agent名称
    custom_prompt TEXT, -- 用户自定义系统提示词
    config JSONB, -- 用户个性化配置
    is_favorite BOOLEAN DEFAULT FALSE,
    usage_count INTEGER DEFAULT 0,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id) REFERENCES agent_definitions(id) ON DELETE CASCADE,
    UNIQUE(user_id, agent_id)
);

-- 添加索引
CREATE INDEX idx_user_agent_configs_user_id ON user_agent_configs(user_id);
CREATE INDEX idx_user_agent_configs_agent_id ON user_agent_configs(agent_id);
CREATE INDEX idx_user_agent_configs_enabled ON user_agent_configs(enabled);
CREATE INDEX idx_user_agent_configs_favorite ON user_agent_configs(is_favorite);

-- Agent会话表 - 多Agent环境下的会话管理
CREATE TABLE agent_sessions (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    session_name VARCHAR(200),
    current_agent_id VARCHAR(255),
    session_type VARCHAR(50) DEFAULT 'single', -- single, multi, collaboration
    status VARCHAR(20) DEFAULT 'active', -- active, paused, completed, archived
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (current_agent_id) REFERENCES agent_definitions(id) ON DELETE SET NULL
);

-- 添加索引
CREATE INDEX idx_agent_sessions_user_id ON agent_sessions(user_id);
CREATE INDEX idx_agent_sessions_current_agent ON agent_sessions(current_agent_id);
CREATE INDEX idx_agent_sessions_status ON agent_sessions(status);

-- Agent会话消息表 - 扩展原有conversations，支持Agent标识
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS agent_id VARCHAR(255);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS session_id VARCHAR(255);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS conversation_type VARCHAR(50) DEFAULT 'single_agent';

-- 添加外键和索引
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_conversations_agent_id') THEN
        ALTER TABLE conversations ADD CONSTRAINT fk_conversations_agent_id 
        FOREIGN KEY (agent_id) REFERENCES agent_definitions(id) ON DELETE SET NULL;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints WHERE constraint_name = 'fk_conversations_session_id') THEN
        ALTER TABLE conversations ADD CONSTRAINT fk_conversations_session_id 
        FOREIGN KEY (session_id) REFERENCES agent_sessions(id) ON DELETE SET NULL;
    END IF;
END $$;

-- 为conversations表添加索引
CREATE INDEX IF NOT EXISTS idx_conversations_agent_id ON conversations(agent_id);
CREATE INDEX IF NOT EXISTS idx_conversations_session_id ON conversations(session_id);

-- Agent协作记录表 - Agent间协作和任务委派
CREATE TABLE agent_collaborations (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id VARCHAR(255) NOT NULL,
    initiator_agent_id VARCHAR(255) NOT NULL,
    target_agent_id VARCHAR(255) NOT NULL,
    task_type VARCHAR(50) NOT NULL, -- delegate, consult, review, etc.
    task_description TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending', -- pending, in_progress, completed, failed
    result TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES agent_sessions(id) ON DELETE CASCADE,
    FOREIGN KEY (initiator_agent_id) REFERENCES agent_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (target_agent_id) REFERENCES agent_definitions(id) ON DELETE CASCADE
);

-- 添加索引
CREATE INDEX idx_agent_collaborations_session_id ON agent_collaborations(session_id);
CREATE INDEX idx_agent_collaborations_initiator ON agent_collaborations(initiator_agent_id);
CREATE INDEX idx_agent_collaborations_target ON agent_collaborations(target_agent_id);
CREATE INDEX idx_agent_collaborations_status ON agent_collaborations(status);

-- Agent使用统计表
CREATE TABLE agent_usage_stats (
    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR(255) NOT NULL,
    agent_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255),
    usage_date DATE NOT NULL,
    message_count INTEGER DEFAULT 0,
    token_count INTEGER DEFAULT 0,
    tool_calls INTEGER DEFAULT 0,
    response_time_avg INTEGER DEFAULT 0, -- 平均响应时间(毫秒)
    satisfaction_score INTEGER, -- 1-5分满意度评分
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (agent_id) REFERENCES agent_definitions(id) ON DELETE CASCADE,
    FOREIGN KEY (session_id) REFERENCES agent_sessions(id) ON DELETE SET NULL,
    UNIQUE(user_id, agent_id, usage_date)
);

-- 添加索引
CREATE INDEX idx_agent_usage_stats_user_date ON agent_usage_stats(user_id, usage_date);
CREATE INDEX idx_agent_usage_stats_agent_date ON agent_usage_stats(agent_id, usage_date);

-- 插入系统预定义Agent
INSERT INTO agent_definitions (id, code, name, display_name, description, category, system_prompt, is_system_agent, created_by) VALUES
-- 通用助手
('agent-general', 'GENERAL_ASSISTANT', 'general_assistant', '通用AI助手', '全能的AI助手，可以协助处理各种任务和问题', 'general', 
'你是一个友好、专业的AI助手。你可以帮助用户解决各种问题，提供准确的信息和有用的建议。请始终保持礼貌和耐心。', TRUE, 'system'),

-- 代码助手
('agent-code', 'CODE_ASSISTANT', 'code_assistant', '代码开发助手', '专门协助编程开发、代码审查和技术问题解决的AI助手', 'development', 
'你是一个专业的代码开发助手。你精通多种编程语言和开发框架，可以帮助用户编写、调试、优化代码，解释技术概念，提供最佳实践建议。请确保提供的代码是安全、高效和可维护的。', TRUE, 'system'),

-- 写作助手
('agent-writer', 'WRITING_ASSISTANT', 'writing_assistant', '写作创意助手', '专注于写作、编辑和内容创作的AI助手', 'creative', 
'你是一个专业的写作助手。你擅长各种类型的写作，包括技术文档、创意写作、商业文案等。你可以帮助用户改进文章结构、语言表达，提供创意灵感和写作建议。', TRUE, 'system'),

-- 数据分析师
('agent-analyst', 'DATA_ANALYST', 'data_analyst', '数据分析专家', '专门处理数据分析、统计和可视化任务的AI助手', 'analysis', 
'你是一个专业的数据分析师。你精通统计学、数据挖掘和数据可视化技术。你可以帮助用户分析数据、发现模式、创建图表和提供基于数据的洞察。', TRUE, 'system'),

-- 研究助手
('agent-research', 'RESEARCH_ASSISTANT', 'research_assistant', '学术研究助手', '协助学术研究、文献综述和知识整理的AI助手', 'research', 
'你是一个专业的研究助手。你可以帮助用户进行文献调研、整理知识、总结观点、分析理论。你具有严谨的学术态度，注重信息的准确性和可靠性。', TRUE, 'system'),

-- 客服助手
('agent-support', 'CUSTOMER_SUPPORT', 'customer_support', '客户服务助手', '专门处理客户咨询和技术支持的AI助手', 'support', 
'你是一个专业的客户服务助手。你耐心友好，能够理解客户需求，提供准确的解答和有效的解决方案。你始终以客户满意为目标。', TRUE, 'system');

-- 为系统Agent配置默认工具
INSERT INTO agent_tool_configs (agent_id, tool_id, enabled, priority) 
SELECT ad.id, td.id, TRUE, 
    CASE 
        WHEN ad.code = 'CODE_ASSISTANT' AND td.code = 'TERMINAL' THEN 1
        WHEN ad.code = 'CODE_ASSISTANT' AND td.code = 'WEB_SCRAPER' THEN 2
        WHEN ad.code = 'DATA_ANALYST' AND td.code = 'CALCULATOR' THEN 1
        WHEN ad.code = 'RESEARCH_ASSISTANT' AND td.code = 'KNOWLEDGE_BASE' THEN 1
        WHEN ad.code = 'RESEARCH_ASSISTANT' AND td.code = 'WEB_SCRAPER' THEN 2
        ELSE 3
    END
FROM agent_definitions ad
CROSS JOIN tool_definitions td
WHERE ad.is_system_agent = TRUE
AND (
    (ad.code = 'GENERAL_ASSISTANT') OR
    (ad.code = 'CODE_ASSISTANT' AND td.code IN ('TERMINAL', 'WEB_SCRAPER', 'CALCULATOR')) OR
    (ad.code = 'WRITING_ASSISTANT' AND td.code IN ('KNOWLEDGE_BASE', 'WEB_SCRAPER')) OR
    (ad.code = 'DATA_ANALYST' AND td.code IN ('CALCULATOR', 'WEB_SCRAPER')) OR
    (ad.code = 'RESEARCH_ASSISTANT' AND td.code IN ('KNOWLEDGE_BASE', 'WEB_SCRAPER')) OR
    (ad.code = 'CUSTOMER_SUPPORT' AND td.code IN ('KNOWLEDGE_BASE'))
);

-- 创建用户Agent配置的默认数据（为所有现有用户）
INSERT INTO user_agent_configs (user_id, agent_id, enabled, is_favorite)
SELECT u.id, ad.id, TRUE, 
    CASE WHEN ad.code = 'GENERAL_ASSISTANT' THEN TRUE ELSE FALSE END
FROM users u
CROSS JOIN agent_definitions ad
WHERE ad.is_system_agent = TRUE
ON CONFLICT (user_id, agent_id) DO NOTHING;

-- 添加Agent权限到权限表
INSERT INTO permissions (id, code, name, description, resource_type, operation, created_at) VALUES
('perm-agent-list', 'AGENT:LIST', '查看Agent列表', '查看可用的AI助手列表', 'AGENT', 'READ', NOW()),
('perm-agent-use', 'AGENT:USE', '使用Agent', '与AI助手进行对话', 'AGENT', 'EXECUTE', NOW()),
('perm-agent-config', 'AGENT:CONFIG', '配置Agent', '个性化配置AI助手', 'AGENT', 'UPDATE', NOW()),
('perm-agent-create', 'AGENT:CREATE', '创建Agent', '创建自定义AI助手', 'AGENT', 'CREATE', NOW()),
('perm-agent-manage', 'AGENT:MANAGE', '管理Agent', '管理AI助手定义和配置', 'AGENT', 'ADMIN', NOW()),
('perm-multi-agent', 'MULTI_AGENT:USE', '多Agent协作', '使用多个AI助手协作功能', 'AGENT', 'EXECUTE', NOW())
ON CONFLICT (code) DO NOTHING;

-- 为用户角色分配Agent权限
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.code = 'USER' 
AND p.code IN ('AGENT:LIST', 'AGENT:USE', 'AGENT:CONFIG')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 为管理员角色分配所有Agent权限
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r, permissions p
WHERE r.code = 'ADMIN' 
AND p.code LIKE 'AGENT:%' OR p.code LIKE 'MULTI_AGENT:%'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 添加注释
COMMENT ON TABLE agent_definitions IS 'AI助手定义表，存储不同类型的AI助手配置';
COMMENT ON TABLE agent_tool_configs IS 'Agent工具配置表，定义每个Agent可用的工具';
COMMENT ON TABLE agent_knowledge_permissions IS 'Agent知识库权限表，控制Agent对知识库的访问';
COMMENT ON TABLE user_agent_configs IS '用户Agent配置表，存储用户对Agent的个人设置';
COMMENT ON TABLE agent_sessions IS 'Agent会话表，管理多Agent环境下的对话会话';
COMMENT ON TABLE agent_collaborations IS 'Agent协作记录表，记录Agent间的任务委派和协作';
COMMENT ON TABLE agent_usage_stats IS 'Agent使用统计表，记录用户对Agent的使用情况';