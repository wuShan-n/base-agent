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


