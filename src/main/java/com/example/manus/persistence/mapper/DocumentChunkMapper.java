package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.DocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunk> {

    @Select("SELECT * FROM document_chunks WHERE document_id = #{documentId} ORDER BY chunk_index")
    List<DocumentChunk> selectByDocumentId(String documentId);

    @Select("SELECT * FROM document_chunks WHERE knowledge_base_id = #{knowledgeBaseId}")
    List<DocumentChunk> selectByKnowledgeBaseId(String knowledgeBaseId);

    @Select("SELECT COUNT(*) FROM document_chunks WHERE document_id = #{documentId}")
    int countByDocumentId(String documentId);
}