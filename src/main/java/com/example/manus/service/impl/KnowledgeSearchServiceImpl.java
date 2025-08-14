package com.example.manus.service.impl;

import com.example.manus.service.KnowledgeSearchService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Service
@RequiredArgsConstructor
public class KnowledgeSearchServiceImpl implements KnowledgeSearchService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;

    @Override
    public List<Content> searchByKnowledgeBase(String knowledgeBaseId, String query, int maxResults) {
        Filter filter = metadataKey("knowledge_base_id").isEqualTo(knowledgeBaseId);
        return searchWithFilter(query, filter, maxResults);
    }

    @Override
    public List<Content> searchByUser(String userId, String query, int maxResults) {
        Filter filter = metadataKey("user_id").isEqualTo(userId);
        return searchWithFilter(query, filter, maxResults);
    }

    @Override
    public List<Content> searchPublic(String query, int maxResults) {
        // 这里需要结合数据库查询来获取公开的知识库ID
        // 简化实现，实际应该先查询公开的知识库ID列表
        return searchAll(query, maxResults);
    }

    @Override
    public List<Content> searchAll(String query, int maxResults) {
        return searchWithFilter(query, null, maxResults);
    }

    @Override
    public List<Content> searchWithFilters(String query, String knowledgeBaseId, String userId, boolean includePublic, int maxResults) {
        Filter filter = null;

        if (knowledgeBaseId != null) {
            filter = metadataKey("knowledge_base_id").isEqualTo(knowledgeBaseId);
        } else if (userId != null && !includePublic) {
            filter = metadataKey("user_id").isEqualTo(userId);
        }

        return searchWithFilter(query, filter, maxResults);
    }

    private List<Content> searchWithFilter(String query, Filter filter, int maxResults) {
        try {
            // 生成查询向量
            var queryEmbedding = embeddingModel.embed(query).content();

            // 构建搜索请求
            EmbeddingSearchRequest.Builder requestBuilder = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(maxResults)
                    .minScore(0.3); // 最小相似度阈值

            if (filter != null) {
                requestBuilder.filter(filter);
            }

            // 执行搜索
            EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(requestBuilder.build());

            // 转换结果
            return searchResult.matches().stream()
                    .map(match -> Content.from(match.embedded()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("知识库搜索失败: " + e.getMessage(), e);
        }
    }
}