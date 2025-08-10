package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("conversations")
public class Conversation {

    @TableId
    private String id;

    private String userId;

    private String summary;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
