package com.example.manus.util;


import com.example.manus.service.ConversationPersistenceService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseChatMemoryStore implements ChatMemoryStore {

    private final ConversationPersistenceService persistenceService;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return ChatMessageConverter.fromEntities(persistenceService.loadHistory(memoryId.toString()));
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String conversationId = memoryId.toString();
        persistenceService.findOrCreateConversation(conversationId, "default-user");

        int currentDbCount = persistenceService.loadHistory(conversationId).size();
        if (messages.size() > currentDbCount) {
             List<ChatMessage> newMessages = messages.subList(currentDbCount, messages.size());
             for(ChatMessage newMessage : newMessages) {
                 persistenceService.appendMessage(
                     ChatMessageConverter.toEntity(newMessage, conversationId)
                 );
             }
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {

    }
}
