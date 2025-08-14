package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.ToolDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ToolDefinitionMapper extends BaseMapper<ToolDefinition> {

    @Select("SELECT * FROM tool_definitions WHERE status = 1 ORDER BY sort_order, created_at")
    List<ToolDefinition> selectEnabledTools();

    @Select("SELECT * FROM tool_definitions WHERE category = #{category} AND status = 1 " +
            "ORDER BY sort_order, created_at")
    List<ToolDefinition> selectByCategory(String category);

    @Select("SELECT * FROM tool_definitions WHERE code = #{code} AND status = 1")
    ToolDefinition selectByCode(String code);

    @Select("SELECT DISTINCT category FROM tool_definitions WHERE status = 1")
    List<String> selectCategories();
}