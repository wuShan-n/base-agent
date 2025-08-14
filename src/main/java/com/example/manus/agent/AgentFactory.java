package com.example.manus.agent;

import com.example.manus.tool.CalculatorTool;
import com.example.manus.tool.KnowledgeBaseTool;
import com.example.manus.tool.TerminalTool;
import com.example.manus.tool.WebScraperTool;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AgentFactory {

    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryProvider chatMemoryProvider;
    private final KnowledgeBaseTool knowledgeBaseTool;
    private final CalculatorTool calculatorTool;
    private final WebScraperTool webScraperTool;
    private final TerminalTool terminalTool;

    private final ApplicationContext context;

    private static final Map<String, Class<?>> TOOL_CLASSES = Map.of(
            "knowledgeBaseTool", KnowledgeBaseTool.class,
            "calculatorTool", CalculatorTool.class,
            "webScraperTool", WebScraperTool.class,
            "terminalTool", TerminalTool.class
    );

    @Bean
    public Assistant assistant() {
        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .build();
    }

    /**
     * 动态创建一个 Assistant 实例，该实例只包含前端请求启用的工具。
     * @param enabledToolNames 前端传来的启用的工具Bean名称列表 (e.g., ["calculatorTool", "webScraperTool"])
     * @return 一个定制化的 Assistant 实例
     */
    public Assistant createAssistant(List<String> enabledToolNames) {
        List<Object> enabledTools = new ArrayList<>();
        if (enabledToolNames != null) {
            for (String toolName : enabledToolNames) {
                TOOL_CLASSES.keySet().stream()
                        .filter(key -> key.equalsIgnoreCase(toolName))
                        .findFirst()
                        .ifPresent(matchingKey -> {
                            // 从 Spring 上下文中获取工具的 Bean 实例
                            enabledTools.add(context.getBean(TOOL_CLASSES.get(matchingKey)));
                        });
            }
        }

        // 总是默认启用知识库工具
        if (enabledTools.stream().noneMatch(tool -> tool instanceof KnowledgeBaseTool)) {
            enabledTools.add(context.getBean(KnowledgeBaseTool.class));
        }


        return AiServices.builder(Assistant.class)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(enabledTools)
                .build();
    }




}
