CREATE TABLE conversations
(
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

CREATE INDEX ON chat_messages (conversation_id);


CREATE OR REPLACE FUNCTION update_conversation_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    UPDATE conversations SET updated_at = now() WHERE id = NEW.conversation_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_conversation_timestamp
    AFTER INSERT
    ON chat_messages
    FOR EACH ROW
EXECUTE FUNCTION update_conversation_updated_at();
