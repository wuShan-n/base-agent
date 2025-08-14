-- 用户知识库管理系统数据库表结构

-- 创建知识库表
CREATE TABLE knowledge_bases (
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_public INTEGER NOT NULL DEFAULT 0, -- 0:私有 1:公开
    status INTEGER NOT NULL DEFAULT 1, -- 0:禁用 1:启用
    document_count INTEGER NOT NULL DEFAULT 0,
    total_size BIGINT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建知识库文档表
CREATE TABLE knowledge_documents (
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    knowledge_base_id VARCHAR(255) NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT NOT NULL,
    file_path TEXT NOT NULL,
    summary TEXT,
    process_status INTEGER NOT NULL DEFAULT 0, -- 0:待处理 1:处理中 2:已完成 3:失败
    chunk_count INTEGER DEFAULT 0,
    user_id VARCHAR(255) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 创建文档分片表
CREATE TABLE document_chunks (
    id  VARCHAR(255) PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id VARCHAR(255) NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    knowledge_base_id VARCHAR(255) NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_size INTEGER NOT NULL,
    embedding_id VARCHAR(255), -- 关联向量表的ID
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 修改现有的向量表，增加知识库关联
-- ALTER TABLE document_embeddings ADD COLUMN knowledge_base_id VARCHAR(255);
-- ALTER TABLE document_embeddings ADD COLUMN chunk_id VARCHAR(255);
-- ALTER TABLE document_embeddings ADD COLUMN user_id VARCHAR(255);


-- 创建用于更新知识库统计信息（文档数、总大小）的特定触发器
CREATE OR REPLACE FUNCTION update_kb_stats()
    RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE knowledge_bases
        SET document_count = document_count + 1,
            total_size = total_size + NEW.file_size,
            last_updated = now()
        WHERE id = NEW.knowledge_base_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE knowledge_bases
        SET document_count = document_count - 1,
            total_size = total_size - OLD.file_size,
            last_updated = now()
        WHERE id = OLD.knowledge_base_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 将统计触发器绑定到文档表
CREATE TRIGGER trigger_update_kb_stats
    AFTER INSERT OR DELETE ON knowledge_documents
    FOR EACH ROW
EXECUTE FUNCTION update_kb_stats();

-- 初始化知识库相关的权限数据
INSERT INTO permissions (name, code, resource, action, description) VALUES
    ('知识库管理', 'KNOWLEDGE_BASE_MANAGE', 'knowledge_base', '*', '知识库管理相关权限')
ON CONFLICT (code) DO NOTHING;

-- 使用业务Code为角色分配权限，不再依赖硬编码ID

-- 为 'SUPER_ADMIN' 角色分配知识库管理权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'SUPER_ADMIN' AND p.code = 'KNOWLEDGE_BASE_MANAGE'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 为 'USER' 角色分配基础的文档管理权限 (根据原脚本逻辑保留)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'USER' AND p.code = 'DOCUMENT_MANAGE'
ON CONFLICT (role_id, permission_id) DO NOTHING;
