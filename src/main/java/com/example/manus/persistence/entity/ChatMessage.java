package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName(value = "chat_messages")
public class ChatMessage {

    @TableId
    private String id;
    private String conversationId;
    private ChatMessageRole role;
    private String content;
    private LocalDateTime createdAt;
}
