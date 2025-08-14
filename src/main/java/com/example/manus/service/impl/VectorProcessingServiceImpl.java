package com.example.manus.service.impl;

import com.example.manus.persistence.entity.DocumentChunk;
import com.example.manus.persistence.entity.KnowledgeDocument;
import com.example.manus.persistence.mapper.DocumentChunkMapper;
import com.example.manus.persistence.mapper.KnowledgeDocumentMapper;
import com.example.manus.service.VectorProcessingService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VectorProcessingServiceImpl implements VectorProcessingService {

    private final KnowledgeDocumentMapper knowledgeDocumentMapper;
    private final DocumentChunkMapper documentChunkMapper;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Override
    @Async
    public int processDocument(String documentId) {
        KnowledgeDocument document = knowledgeDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new RuntimeException("文档不存在: " + documentId);
        }

        try {
            // 加载文档
            Document langchainDocument = FileSystemDocumentLoader.loadDocument(Paths.get(document.getFilePath()));

            // 分割文档
            DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(2000, 200);
            List<TextSegment> segments = splitter.split(langchainDocument);

            // 处理每个分片
            int chunkCount = 0;
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);

                // 创建分片记录
                DocumentChunk chunk = new DocumentChunk();
                chunk.setId(UUID.randomUUID().toString());
                chunk.setDocumentId(documentId);
                chunk.setKnowledgeBaseId(document.getKnowledgeBaseId());
                chunk.setContent(segment.text());
                chunk.setChunkIndex(i);
                chunk.setChunkSize(segment.text().length());
                chunk.setCreatedAt(LocalDateTime.now());

                // 生成向量嵌入
                String embeddingId = storeEmbedding(segment, document.getKnowledgeBaseId(), chunk.getId(), document.getUserId());
                chunk.setEmbeddingId(embeddingId);

                // 保存分片
                documentChunkMapper.insert(chunk);
                chunkCount++;
            }

            return chunkCount;

        } catch (Exception e) {
            throw new RuntimeException("文档处理失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void reprocessDocument(String documentId) {
        // 先删除现有的向量数据
        deleteDocumentVectors(documentId);

        // 重新处理文档
        processDocument(documentId);
    }

    @Override
    public void deleteDocumentVectors(String documentId) {
        // 获取文档的所有分片
        List<DocumentChunk> chunks = documentChunkMapper.selectByDocumentId(documentId);

        // 删除向量存储中的数据
        for (DocumentChunk chunk : chunks) {
            if (chunk.getEmbeddingId() != null) {
                try {
                    embeddingStore.removeAll(List.of(chunk.getEmbeddingId()));
                } catch (Exception e) {
                    System.err.println("删除向量失败: " + e.getMessage());
                }
            }
        }

        // 删除分片记录
        chunks.forEach(chunk -> documentChunkMapper.deleteById(chunk.getId()));
    }

    @Override
    public void deleteKnowledgeBaseVectors(String knowledgeBaseId) {
        // 获取知识库的所有分片
        List<DocumentChunk> chunks = documentChunkMapper.selectByKnowledgeBaseId(knowledgeBaseId);

        // 删除向量存储中的数据
        for (DocumentChunk chunk : chunks) {
            if (chunk.getEmbeddingId() != null) {
                try {
                    embeddingStore.removeAll(List.of(chunk.getEmbeddingId()));
                } catch (Exception e) {
                    System.err.println("删除向量失败: " + e.getMessage());
                }
            }
        }

        // 删除分片记录
        chunks.forEach(chunk -> documentChunkMapper.deleteById(chunk.getId()));
    }

    private String storeEmbedding(TextSegment segment, String knowledgeBaseId, String chunkId, String userId) {
        try {
            // 添加元数据
            segment = segment.toBuilder()
                    .metadata("knowledge_base_id", knowledgeBaseId)
                    .metadata("chunk_id", chunkId)
                    .metadata("user_id", userId)
                    .build();

            // 存储到向量数据库
            String embeddingId = embeddingStore.add(embeddingModel.embed(segment).content(), segment);
            return embeddingId;

        } catch (Exception e) {
            throw new RuntimeException("向量存储失败: " + e.getMessage(), e);
        }
    }
}