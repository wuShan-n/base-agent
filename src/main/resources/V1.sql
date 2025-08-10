-- 如果旧对象存在，先清理环境 (CASCADE会一并删除关联的触发器等)
DROP TABLE IF EXISTS chat_messages, conversations CASCADE;
DROP TYPE IF EXISTS chat_message_role;
DROP FUNCTION IF EXISTS update_conversation_updated_at;


-- 第一步: 创建自定义ENUM类型，用于数据约束和性能优化
CREATE TYPE chat_message_role AS ENUM (
    'SYSTEM',
    'USER',
    'AI',
    'TOOL_EXECUTION_RESULT'
    );


-- 第二步: 创建核心数据表，使用内置函数和简洁语法
CREATE TABLE conversations
(
    -- 使用内置函数 gen_random_uuid()，无需任何扩展
    id         VARCHAR(255) PRIMARY KEY  ,
    user_id    VARCHAR(255),
    summary    TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE chat_messages
(
    id              VARCHAR(255) PRIMARY KEY,
    conversation_id VARCHAR(255)               NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    role            VARCHAR(255) NOT NULL,
    content         TEXT              NOT NULL,
    created_at      TIMESTAMP         NOT NULL DEFAULT now()
);

-- 第三步: 创建索引以优化查询性能
CREATE INDEX ON chat_messages (conversation_id);


-- 第四步: 创建函数和触发器，以最标准的方式自动更新时间戳
CREATE OR REPLACE FUNCTION update_conversation_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    -- 将触发操作的会话的updated_at更新为当前时间
    UPDATE conversations SET updated_at = now() WHERE id = NEW.conversation_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_conversation_timestamp
    -- 在向chat_messages表插入新行后触发
    AFTER INSERT
    ON chat_messages
    FOR EACH ROW
EXECUTE FUNCTION update_conversation_updated_at();
