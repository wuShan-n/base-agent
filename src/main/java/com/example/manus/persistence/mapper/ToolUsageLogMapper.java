package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.ToolUsageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ToolUsageLogMapper extends BaseMapper<ToolUsageLog> {

    @Select("SELECT tool_id, COUNT(*) as usage_count " +
            "FROM tool_usage_logs " +
            "WHERE user_id = #{userId} AND created_at >= #{startTime} " +
            "GROUP BY tool_id " +
            "ORDER BY usage_count DESC")
    List<Map<String, Object>> selectUsageStatsByUser(String userId, LocalDateTime startTime);

    @Select("SELECT tool_id, COUNT(*) as usage_count " +
            "FROM tool_usage_logs " +
            "WHERE created_at >= #{startTime} " +
            "GROUP BY tool_id " +
            "ORDER BY usage_count DESC")
    List<Map<String, Object>> selectGlobalUsageStats(LocalDateTime startTime);

    @Select("SELECT * FROM tool_usage_logs " +
            "WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<ToolUsageLog> selectRecentByUserId(String userId, int limit);
}