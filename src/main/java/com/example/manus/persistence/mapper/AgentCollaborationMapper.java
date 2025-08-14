package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.AgentCollaboration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AgentCollaborationMapper extends BaseMapper<AgentCollaboration> {

    @Select("SELECT * FROM agent_collaborations WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    List<AgentCollaboration> selectBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT * FROM agent_collaborations WHERE initiator_agent_id = #{agentId} OR target_agent_id = #{agentId} " +
            "ORDER BY created_at DESC")
    List<AgentCollaboration> selectByAgentId(@Param("agentId") String agentId);

    @Select("SELECT * FROM agent_collaborations WHERE status = #{status} ORDER BY created_at ASC")
    List<AgentCollaboration> selectByStatus(@Param("status") String status);

    @Update("UPDATE agent_collaborations SET status = #{status}, result = #{result}, completed_at = NOW() " +
            "WHERE id = #{collaborationId}")
    Integer completeCollaboration(@Param("collaborationId") String collaborationId, 
                                 @Param("status") String status, 
                                 @Param("result") String result);

    @Select("SELECT COUNT(*) FROM agent_collaborations WHERE session_id = #{sessionId}")
    Integer countBySessionId(@Param("sessionId") String sessionId);

    @Select("SELECT COUNT(*) FROM agent_collaborations WHERE target_agent_id = #{agentId} AND status = 'pending'")
    Integer countPendingTasksByAgentId(@Param("agentId") String agentId);

    @Select("SELECT * FROM agent_collaborations WHERE target_agent_id = #{agentId} AND status = 'pending' " +
            "ORDER BY created_at ASC LIMIT #{limit}")
    List<AgentCollaboration> selectPendingTasksByAgentId(@Param("agentId") String agentId, @Param("limit") Integer limit);
}