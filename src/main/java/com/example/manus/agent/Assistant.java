package com.example.manus.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface Assistant {

    Flux<String> chat(@MemoryId String conversationId, @UserMessage String userMessage);
}
