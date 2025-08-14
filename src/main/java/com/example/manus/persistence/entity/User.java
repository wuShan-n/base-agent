package com.example.manus.persistence.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId
    private String id;

    private String username;

    private String password;

    private String email;

    private String nickname;

    private String avatar;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}