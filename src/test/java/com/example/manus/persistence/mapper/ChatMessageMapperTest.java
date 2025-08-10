package com.example.manus.persistence.mapper;

import com.example.manus.persistence.entity.ChatMessage;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * Created by wuShan on 2025/8/10
 */
@SpringBootTest
class ChatMessageMapperTest {
    @Resource
    ChatMessageMapper chatMessageMapper;
    @Test
    void t1(){
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent("asdhsdgb");
        chatMessage.setId("12345");
        chatMessage.setConversationId("123456");
        int insert = chatMessageMapper.insert(chatMessage);
        System.out.println(insert);
    }

}
