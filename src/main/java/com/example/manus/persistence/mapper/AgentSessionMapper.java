package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.AgentSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AgentSessionMapper extends BaseMapper<AgentSession> {

    @Select("SELECT * FROM agent_sessions WHERE user_id = #{userId} AND status = 'active' ORDER BY updated_at DESC")
    List<AgentSession> selectActiveByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM agent_sessions WHERE user_id = #{userId} ORDER BY updated_at DESC LIMIT #{limit}")
    List<AgentSession> selectRecentByUserId(@Param("userId") String userId, @Param("limit") Integer limit);

    @Select("SELECT * FROM agent_sessions WHERE user_id = #{userId} AND session_type = #{sessionType} " +
            "ORDER BY updated_at DESC")
    List<AgentSession> selectByUserAndType(@Param("userId") String userId, @Param("sessionType") String sessionType);

    @Update("UPDATE agent_sessions SET status = 'completed', updated_at = NOW() WHERE id = #{sessionId}")
    Integer completeSession(@Param("sessionId") String sessionId);

    @Update("UPDATE agent_sessions SET current_agent_id = #{agentId}, updated_at = NOW() WHERE id = #{sessionId}")
    Integer switchAgent(@Param("sessionId") String sessionId, @Param("agentId") String agentId);

    @Select("SELECT COUNT(*) FROM agent_sessions WHERE user_id = #{userId} AND status = 'active'")
    Integer countActiveByUserId(@Param("userId") String userId);

    @Update("UPDATE agent_sessions SET status = 'archived', updated_at = NOW() " +
            "WHERE user_id = #{userId} AND status = 'completed' AND updated_at < NOW() - INTERVAL '30 days'")
    Integer archiveOldSessions(@Param("userId") String userId);
}