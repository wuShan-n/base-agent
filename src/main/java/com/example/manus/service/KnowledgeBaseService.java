package com.example.manus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.persistence.entity.KnowledgeBase;

import java.util.List;

public interface KnowledgeBaseService {

    KnowledgeBase createKnowledgeBase(KnowledgeBase knowledgeBase);

    KnowledgeBase updateKnowledgeBase(KnowledgeBase knowledgeBase);

    void deleteKnowledgeBase(String id);

    KnowledgeBase getKnowledgeBaseById(String id);

    List<KnowledgeBase> getUserKnowledgeBases(String userId);

    List<KnowledgeBase> getPublicKnowledgeBases();

    Page<KnowledgeBase> searchKnowledgeBases(long current, long size, String keyword, String userId, Integer isPublic);

    boolean hasAccessPermission(String knowledgeBaseId, String userId);

    void updateKnowledgeBaseStats(String knowledgeBaseId);
}