package com.example.manus.util;


import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 最终的、整合了“召回”与“精排”的检索器。
 * 这是我们自定义的 RAG 流程的顶层实现。
 */
@RequiredArgsConstructor
public class FinalRetriever implements ContentRetriever {

    private final ContentRetriever recaller; // 召回层，例如我们的 ManualMultiQueryRetriever
    private final HttpRankingModel reranker;  // 精排层，我们的 HTTP 客户端
    private final int maxResults;             // 精排后最终返回的文档数量

    @Override
    public List<Content> retrieve(Query query) {
        // 1. 召回层：使用多视角查询等策略获取一个广泛的候选文档集
        List<Content> recalledContent = recaller.retrieve(query);

        // 如果召回结果为空，直接返回
        if (recalledContent.isEmpty()) {
            return recalledContent;
        }

        System.out.println("召回了 " + recalledContent.size() + " 个候选文档，现在开始精排...");

        // 2. 精排层：调用我们手写的 reranker 对召回的文档进行重新排序
        List<Content> rerankedContent = reranker.rerank(
                query.text(),
                recalledContent.stream().map(Content::textSegment).collect(Collectors.toList())
        ).stream().map(Content::from).toList();

        // 3. 截断：只返回得分最高的 Top-N 个结果
        return rerankedContent.stream()
                .limit(maxResults)
                .collect(Collectors.toList());
    }
}
