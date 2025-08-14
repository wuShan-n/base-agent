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
public class MultiQueryRetriever implements ContentRetriever {
    private static final String GENERATE_QUERIES_PROMPT_TEMPLATE =
            """
                    请根据以下问题，生成 %d 个用于在向量数据库中进行检索的、不同角度的查询。
                    请确保这些查询在语义上与原问题相似，但使用不同的措辞或关注不同的方面。
                    请仅返回查询列表，每个查询占一行，不要包含任何其他说明或编号。
                    原始问题: %s
                    """;
    private final ChatModel chatModel;
    private final int queryCount;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final ContentRetriever basicRetriever;

    @Override
    public List<Content> retrieve(Query query) {

        System.out.println(" ManualMultiQueryRetriever is called. Original query: " + query.text());

        // 1. 使用 LLM 生成多个查询变体
        String prompt = String.format(GENERATE_QUERIES_PROMPT_TEMPLATE, queryCount, query.text());
        // --- 修正：使用 chat() 方法代替 generate() ---
        AiMessage response = chatModel.chat(UserMessage.from(prompt)).aiMessage();
        List<String> rewrittenQueries = Arrays.asList(response.text().split("\n"));

        System.out.println(" Generated queries: " + rewrittenQueries);

        // 2. 将原始查询和生成的查询合并到一个列表中
        Set<String> allQueries = new HashSet<>(rewrittenQueries);
        allQueries.add(query.text()); // 添加原始查询

        // 3. 并行执行所有查询的检索
        List<CompletableFuture<List<Content>>> futures = allQueries.stream()
                .map(q -> CompletableFuture.supplyAsync(() -> basicRetriever.retrieve(Query.from(q)), executorService))
                .toList();

        // 4. 等待所有检索完成，并将结果合并、去重
        Set<Content> uniqueContents = new HashSet<>();
        futures.forEach(future -> {
            try {
                uniqueContents.addAll(future.get());
            } catch (Exception e) {
                System.err.println("Error retrieving content: " + e.getMessage());
            }
        });

        System.out.println("Retrieved " + uniqueContents.size() + " unique documents after multi-query rewriting.");
        return List.copyOf(uniqueContents);
    }
}
