package com.example.manus.agent;

import com.example.manus.tool.CalculatorTool;
import com.example.manus.tool.KnowledgeBaseTool;
import com.example.manus.tool.TerminalTool;
import com.example.manus.tool.WebScraperTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AgentFactory {

    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryProvider chatMemoryProvider;
    private final KnowledgeBaseTool knowledgeBaseTool;
    private final CalculatorTool calculatorTool;
    private final WebScraperTool webScraperTool;
    private final TerminalTool terminalTool;

    @Bean
    public Assistant assistant() {
        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

}
