-- Multi-Agent System Database Schema (Final Corrected Version)
-- Compatible with V1, V2, V3, V4 Schemas
-- Created for Manus AI Assistant Platform
-- Version: 5.2

-- -----------------------------------------------------
-- Section 1: Agent & Session Table Definitions
-- -----------------------------------------------------

-- Agent定义表 - 存储不同类型的AI助手定义
CREATE TABLE IF NOT EXISTS agent_definitions (
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

-- Agent工具配置表 - Agent可用的工具配置 (依赖 V4_tools.sql)
CREATE TABLE IF NOT EXISTS agent_tool_configs (
                                                  id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  agent_id VARCHAR(255) NOT NULL REFERENCES agent_definitions(id) ON DELETE CASCADE,
                                                  tool_id VARCHAR(255) NOT NULL REFERENCES tool_definitions(id) ON DELETE CASCADE,
                                                  enabled BOOLEAN DEFAULT TRUE,
                                                  config JSONB,
                                                  priority INTEGER DEFAULT 0,
                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  UNIQUE(agent_id, tool_id)
);

-- Agent知识库权限表 - Agent可访问的知识库 (依赖 V3_knowledge.sql)
CREATE TABLE IF NOT EXISTS agent_knowledge_permissions (
                                                           id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
                                                           agent_id VARCHAR(255) NOT NULL REFERENCES agent_definitions(id) ON DELETE CASCADE,
                                                           knowledge_base_id VARCHAR(255) NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
                                                           access_type VARCHAR(20) NOT NULL DEFAULT 'read', -- read, write, admin
                                                           enabled BOOLEAN DEFAULT TRUE,
                                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                           UNIQUE(agent_id, knowledge_base_id)
);

-- 用户Agent配置表 - 用户对Agent的个人配置 (依赖 V2.sql)
CREATE TABLE IF NOT EXISTS user_agent_configs (
                                                  id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
                                                  user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                                  agent_id VARCHAR(255) NOT NULL REFERENCES agent_definitions(id) ON DELETE CASCADE,
                                                  enabled BOOLEAN DEFAULT TRUE,
                                                  custom_name VARCHAR(100),
                                                  custom_prompt TEXT,
                                                  config JSONB,
                                                  is_favorite BOOLEAN DEFAULT FALSE,
                                                  usage_count INTEGER DEFAULT 0,
                                                  last_used_at TIMESTAMP,
                                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  UNIQUE(user_id, agent_id)
);

-- Agent会话表
CREATE TABLE IF NOT EXISTS agent_sessions (
                                              id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
                                              user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                              session_name VARCHAR(200),
                                              current_agent_id VARCHAR(255) REFERENCES agent_definitions(id) ON DELETE SET NULL,
                                              session_type VARCHAR(50) DEFAULT 'single',
                                              status VARCHAR(20) DEFAULT 'active',
                                              metadata JSONB,
                                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              expires_at TIMESTAMP
);

-- Agent协作记录表
CREATE TABLE IF NOT EXISTS agent_collaborations (
                                                    id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
                                                    session_id VARCHAR(255) NOT NULL REFERENCES agent_sessions(id) ON DELETE CASCADE,
                                                    initiator_agent_id VARCHAR(255) NOT NULL REFERENCES agent_definitions(id) ON DELETE CASCADE,
                                                    target_agent_id VARCHAR(255) NOT NULL REFERENCES agent_definitions(id) ON DELETE CASCADE,
                                                    task_type VARCHAR(50) NOT NULL,
                                                    task_description TEXT NOT NULL,
                                                    status VARCHAR(20) DEFAULT 'pending',
                                                    result TEXT,
                                                    metadata JSONB,
                                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                    completed_at TIMESTAMP
);

-- Agent使用统计表
CREATE TABLE IF NOT EXISTS agent_usage_stats (
                                                 id VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
                                                 user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                                 agent_id VARCHAR(255) NOT NULL REFERENCES agent_definitions(id) ON DELETE CASCADE,
                                                 session_id VARCHAR(255) REFERENCES agent_sessions(id) ON DELETE SET NULL,
                                                 usage_date DATE NOT NULL,
                                                 message_count INTEGER DEFAULT 0,
                                                 token_count INTEGER DEFAULT 0,
                                                 tool_calls INTEGER DEFAULT 0,
                                                 response_time_avg INTEGER DEFAULT 0,
                                                 satisfaction_score INTEGER,
                                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                 UNIQUE(user_id, agent_id, usage_date)
);


-- -----------------------------------------------------
-- Section 2: Table Alterations & Indexing
-- -----------------------------------------------------

-- 扩展原有conversations表 (依赖 V1.sql)
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS agent_id VARCHAR(255);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS session_id VARCHAR(255);
ALTER TABLE conversations ADD COLUMN IF NOT EXISTS conversation_type VARCHAR(50) DEFAULT 'single_agent';


-- [重构] 添加Agent权限到权限表，完全遵循 resource:action 格式
INSERT INTO permissions (name, code, resource, action, description) VALUES
                                                                        ('查看Agent列表', 'agent:read', 'agent', 'read', '查看可用的AI助手列表'),
                                                                        ('使用Agent', 'agent:execute', 'agent', 'execute', '与AI助手进行对话或执行任务'),
                                                                        ('配置Agent', 'agent:update', 'agent', 'update', '个性化配置AI助手'),
                                                                        ('创建Agent', 'agent:create', 'agent', 'create', '创建自定义AI助手'),
                                                                        ('管理Agent', 'agent:manage', 'agent', 'manage', '管理AI助手定义和配置(包含增删改查)'),
                                                                        ('多Agent协作', 'agent:execute_multi', 'agent', 'execute_multi', '使用多个AI助手协作功能')
ON CONFLICT (code) DO NOTHING;

-- [重构] 为角色分配Agent权限，使用新的权限code
-- 为 'USER' 角色分配基础Agent权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'USER'
  AND p.code IN ('agent:read', 'agent:execute', 'agent:update', 'agent:create') -- 允许用户查看、使用、配置和创建
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 为 'ADMIN' 角色分配所有Agent相关权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ADMIN'
  AND p.resource = 'agent' -- 直接分配所有 resource 为 agent 的权限
ON CONFLICT (role_id, permission_id) DO NOTHING;


-- 为系统Agent配置默认工具 (依赖V4.sql)
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
  AND td.status = 1 -- 只关联启用的工具
  AND (
    (ad.code = 'GENERAL_ASSISTANT') OR
    (ad.code = 'CODE_ASSISTANT' AND td.code IN ('TERMINAL', 'WEB_SCRAPER', 'CALCULATOR')) OR
    (ad.code = 'WRITING_ASSISTANT' AND td.code IN ('KNOWLEDGE_BASE', 'WEB_SCRAPER')) OR
    (ad.code = 'DATA_ANALYST' AND td.code IN ('CALCULATOR', 'WEB_SCRAPER')) OR
    (ad.code = 'RESEARCH_ASSISTANT' AND td.code IN ('KNOWLEDGE_BASE', 'WEB_SCRAPER')) OR
    (ad.code = 'CUSTOMER_SUPPORT' AND td.code IN ('KNOWLEDGE_BASE'))
    )
ON CONFLICT (agent_id, tool_id) DO NOTHING;


-- 为所有现有用户创建默认的Agent配置 (依赖V2.sql)
INSERT INTO user_agent_configs (user_id, agent_id, enabled, is_favorite)
SELECT u.id, ad.id, TRUE,
       CASE WHEN ad.code = 'GENERAL_ASSISTANT' THEN TRUE ELSE FALSE END
FROM users u
         CROSS JOIN agent_definitions ad
WHERE ad.is_system_agent = TRUE
ON CONFLICT (user_id, agent_id) DO NOTHING;
