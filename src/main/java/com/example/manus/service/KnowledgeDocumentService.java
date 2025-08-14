package com.example.manus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.manus.persistence.entity.KnowledgeDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface KnowledgeDocumentService {

    KnowledgeDocument uploadDocument(String knowledgeBaseId, MultipartFile file);

    void deleteDocument(String documentId);

    KnowledgeDocument getDocumentById(String documentId);

    List<KnowledgeDocument> getDocumentsByKnowledgeBase(String knowledgeBaseId);

    Page<KnowledgeDocument> searchDocuments(long current, long size, String knowledgeBaseId, String keyword);

    void processDocument(String documentId);

    void updateProcessStatus(String documentId, Integer status, Integer chunkCount);

    boolean hasAccessPermission(String documentId, String userId);
}