package com.example.manus.service;

public interface VectorProcessingService {

    int processDocument(String documentId);

    void reprocessDocument(String documentId);

    void deleteDocumentVectors(String documentId);

    void deleteKnowledgeBaseVectors(String knowledgeBaseId);
}