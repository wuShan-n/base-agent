package com.example.manus.service;

import dev.langchain4j.rag.content.Content;

import java.util.List;

public interface KnowledgeSearchService {

    List<Content> searchByKnowledgeBase(String knowledgeBaseId, String query, int maxResults);

    List<Content> searchByUser(String userId, String query, int maxResults);

    List<Content> searchPublic(String query, int maxResults);

    List<Content> searchAll(String query, int maxResults);

    List<Content> searchWithFilters(String query, String knowledgeBaseId, String userId, boolean includePublic, int maxResults);
}