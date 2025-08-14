package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.AgentToolConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AgentToolConfigMapper extends BaseMapper<AgentToolConfig> {

    @Select("SELECT * FROM agent_tool_configs WHERE agent_id = #{agentId} AND enabled = TRUE ORDER BY priority ASC")
    List<AgentToolConfig> selectEnabledByAgentId(@Param("agentId") String agentId);

    @Select("SELECT atc.* FROM agent_tool_configs atc " +
            "INNER JOIN tool_definitions td ON atc.tool_id = td.id " +
            "WHERE atc.agent_id = #{agentId} AND atc.enabled = TRUE AND td.status = 1 " +
            "ORDER BY atc.priority ASC")
    List<AgentToolConfig> selectAvailableToolsByAgentId(@Param("agentId") String agentId);

    @Select("SELECT * FROM agent_tool_configs WHERE agent_id = #{agentId} AND tool_id = #{toolId}")
    AgentToolConfig selectByAgentAndTool(@Param("agentId") String agentId, @Param("toolId") String toolId);

    @Select("SELECT COUNT(*) FROM agent_tool_configs WHERE agent_id = #{agentId} AND enabled = TRUE")
    Integer countEnabledByAgentId(@Param("agentId") String agentId);
}