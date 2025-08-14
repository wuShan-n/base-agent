package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {

    @Select("SELECT * FROM knowledge_documents WHERE knowledge_base_id = #{knowledgeBaseId} " +
            "ORDER BY created_at DESC")
    List<KnowledgeDocument> selectByKnowledgeBaseId(String knowledgeBaseId);

    @Select("SELECT * FROM knowledge_documents WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC")
    List<KnowledgeDocument> selectByUserId(String userId);

    @Update("UPDATE knowledge_documents SET process_status = #{status}, " +
            "chunk_count = #{chunkCount}, updated_at = now() WHERE id = #{documentId}")
    int updateProcessStatus(String documentId, Integer status, Integer chunkCount);

    @Select("SELECT COUNT(*) FROM knowledge_documents WHERE knowledge_base_id = #{knowledgeBaseId}")
    int countByKnowledgeBaseId(String knowledgeBaseId);

    @Select("SELECT COALESCE(SUM(file_size), 0) FROM knowledge_documents WHERE knowledge_base_id = #{knowledgeBaseId}")
    long sumFileSizeByKnowledgeBaseId(String knowledgeBaseId);
}