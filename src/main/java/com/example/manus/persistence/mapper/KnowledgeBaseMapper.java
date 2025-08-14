package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.KnowledgeBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBase> {

    @Select("SELECT * FROM knowledge_bases WHERE user_id = #{userId} AND status = 1 ORDER BY created_at DESC")
    List<KnowledgeBase> selectByUserId(String userId);

    @Select("SELECT * FROM knowledge_bases WHERE is_public = 1 AND status = 1 ORDER BY created_at DESC")
    List<KnowledgeBase> selectPublicKnowledgeBases();

    @Update("UPDATE knowledge_bases SET document_count = document_count + #{increment}, " +
            "last_updated = now() WHERE id = #{knowledgeBaseId}")
    int updateDocumentCount(String knowledgeBaseId, int increment);

    @Update("UPDATE knowledge_bases SET total_size = total_size + #{sizeIncrement}, " +
            "last_updated = now() WHERE id = #{knowledgeBaseId}")
    int updateTotalSize(String knowledgeBaseId, long sizeIncrement);
}