package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.AgentDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AgentDefinitionMapper extends BaseMapper<AgentDefinition> {

    @Select("SELECT * FROM agent_definitions WHERE status = 1 ORDER BY category, name")
    List<AgentDefinition> selectEnabledAgents();

    @Select("SELECT * FROM agent_definitions WHERE category = #{category} AND status = 1")
    List<AgentDefinition> selectByCategory(@Param("category") String category);

    @Select("SELECT * FROM agent_definitions WHERE code = #{code}")
    AgentDefinition selectByCode(@Param("code") String code);

    @Select("SELECT DISTINCT category FROM agent_definitions WHERE status = 1 ORDER BY category")
    List<String> selectCategories();

    @Select("SELECT * FROM agent_definitions WHERE is_system_agent = TRUE AND status = 1")
    List<AgentDefinition> selectSystemAgents();

    @Select("SELECT * FROM agent_definitions WHERE created_by = #{userId} AND status = 1")
    List<AgentDefinition> selectUserCreatedAgents(@Param("userId") String userId);
}