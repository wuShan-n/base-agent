package com.example.manus.util;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * Created by wuShan on 2025/8/12
 */
@RequiredArgsConstructor
public class HydeContentRetriever implements ContentRetriever {
    private static final String GENERATE_DOCUMENT_PROMPT_TEMPLATE =
            """
            请根据以下问题，生成一个简洁、理想的答案。
            这个答案将被用于在知识库中检索相关信息，所以请确保它在内容和风格上都像一个真实的答案文档。
            请不要在回答中包含任何 "根据您的问题" 或 "这是一个假设性答案" 等说明性文字，直接生成答案本身。
            问题: %s
            """;
    private final ChatModel chatModel;
    private final ContentRetriever basicRetriever;

    @Override
    public List<Content> retrieve(Query query) {
        String prompt = String.format(GENERATE_DOCUMENT_PROMPT_TEMPLATE, query.text());
        AiMessage aiMessage = chatModel.chat(UserMessage.from(prompt)).aiMessage();
        Query from = Query.from(aiMessage.text());
        return basicRetriever.retrieve(from);
    }
}
