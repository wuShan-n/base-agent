-- 用户知识库管理系统数据库表结构
-- 清理旧表
DROP TABLE IF EXISTS document_chunks, knowledge_documents, knowledge_bases CASCADE;

-- 创建知识库表
CREATE TABLE knowledge_bases (
    id VARCHAR(255) PRIMARY KEY,
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
    id VARCHAR(255) PRIMARY KEY,
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
    id VARCHAR(255) PRIMARY KEY,
    document_id VARCHAR(255) NOT NULL REFERENCES knowledge_documents(id) ON DELETE CASCADE,
    knowledge_base_id VARCHAR(255) NOT NULL REFERENCES knowledge_bases(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    chunk_index INTEGER NOT NULL,
    chunk_size INTEGER NOT NULL,
    embedding_id VARCHAR(255), -- 关联向量表的ID
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 修改现有的向量表，增加知识库关联
ALTER TABLE document_embeddings ADD COLUMN knowledge_base_id VARCHAR(255);
ALTER TABLE document_embeddings ADD COLUMN chunk_id VARCHAR(255);
ALTER TABLE document_embeddings ADD COLUMN user_id VARCHAR(255);

-- 创建索引
CREATE INDEX idx_knowledge_bases_user_id ON knowledge_bases(user_id);
CREATE INDEX idx_knowledge_bases_status ON knowledge_bases(status);
CREATE INDEX idx_knowledge_bases_public ON knowledge_bases(is_public);

CREATE INDEX idx_knowledge_documents_kb_id ON knowledge_documents(knowledge_base_id);
CREATE INDEX idx_knowledge_documents_user_id ON knowledge_documents(user_id);
CREATE INDEX idx_knowledge_documents_status ON knowledge_documents(process_status);

CREATE INDEX idx_document_chunks_doc_id ON document_chunks(document_id);
CREATE INDEX idx_document_chunks_kb_id ON document_chunks(knowledge_base_id);
CREATE INDEX idx_document_chunks_embedding ON document_chunks(embedding_id);

CREATE INDEX idx_document_embeddings_kb_id ON document_embeddings(knowledge_base_id);
CREATE INDEX idx_document_embeddings_chunk_id ON document_embeddings(chunk_id);
CREATE INDEX idx_document_embeddings_user_id ON document_embeddings(user_id);

-- 创建知识库统计触发器
CREATE OR REPLACE FUNCTION update_kb_stats()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- 增加文档数量和大小
        UPDATE knowledge_bases 
        SET document_count = document_count + 1,
            total_size = total_size + NEW.file_size,
            last_updated = now()
        WHERE id = NEW.knowledge_base_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        -- 减少文档数量和大小
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

CREATE TRIGGER trigger_update_kb_stats
    AFTER INSERT OR DELETE ON knowledge_documents
    FOR EACH ROW
EXECUTE FUNCTION update_kb_stats();

-- 创建知识库更新时间触发器
CREATE OR REPLACE FUNCTION update_kb_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_kb_updated_at
    BEFORE UPDATE ON knowledge_bases
    FOR EACH ROW
EXECUTE FUNCTION update_kb_updated_at();

-- 创建文档更新时间触发器
CREATE TRIGGER trigger_update_doc_updated_at
    BEFORE UPDATE ON knowledge_documents
    FOR EACH ROW
EXECUTE FUNCTION update_kb_updated_at();

-- 初始化权限数据
INSERT INTO permissions (id, name, code, resource, action, description) VALUES
('6', '知识库管理', 'KNOWLEDGE_BASE_MANAGE', 'knowledge_base', '*', '知识库管理相关权限'),
('7', '文档管理', 'DOCUMENT_MANAGE', 'document', '*', '文档管理相关权限')
ON CONFLICT (id) DO NOTHING;

-- 给超级管理员添加知识库权限
INSERT INTO role_permissions (id, role_id, permission_id) VALUES
('9', '1', '6'),
('10', '1', '7')
ON CONFLICT (id) DO NOTHING;

-- 给普通用户添加基础文档权限
INSERT INTO role_permissions (id, role_id, permission_id) VALUES
('11', '3', '7')
ON CONFLICT (id) DO NOTHING;