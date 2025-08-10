package com.example.manus.service;


import com.example.manus.persistence.entity.ChatMessage;
import com.example.manus.persistence.entity.Conversation;

import java.util.List;

public interface ConversationPersistenceService {


    Conversation findOrCreateConversation(String conversationId, String userId);


    List<ChatMessage> loadHistory(String conversationId);


    void appendMessage(ChatMessage message);
}
