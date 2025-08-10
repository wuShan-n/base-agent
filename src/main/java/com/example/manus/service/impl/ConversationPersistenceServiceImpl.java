package com.example.manus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.manus.persistence.entity.ChatMessage;
import com.example.manus.persistence.entity.Conversation;
import com.example.manus.persistence.mapper.ChatMessageMapper;
import com.example.manus.persistence.mapper.ConversationMapper;
import com.example.manus.service.ConversationPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationPersistenceServiceImpl implements ConversationPersistenceService {

    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public Conversation findOrCreateConversation(String conversationId, String userId) {
        if (conversationId != null) {
            Conversation conversation = conversationMapper.selectById(conversationId);
            if (conversation != null) {
                return conversation;
            }
        }

        Conversation newConversation = new Conversation();
        newConversation.setId(conversationId);
        newConversation.setUserId(userId);
        newConversation.setCreatedAt(LocalDateTime.now());
        newConversation.setUpdatedAt(LocalDateTime.now());
        conversationMapper.insert(newConversation);
        return newConversation;
    }

    @Override
    public List<ChatMessage> loadHistory(String conversationId) {
        return chatMessageMapper.selectList(
            new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreatedAt)
        );
    }

    @Override
    public void appendMessage(ChatMessage message) {
        chatMessageMapper.insert(message);
    }
}
