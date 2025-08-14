package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.UserToolConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserToolConfigMapper extends BaseMapper<UserToolConfig> {

    @Select("SELECT utc.*, td.code, td.display_name, td.category " +
            "FROM user_tool_configs utc " +
            "LEFT JOIN tool_definitions td ON utc.tool_id = td.id " +
            "WHERE utc.user_id = #{userId} AND utc.enabled = true " +
            "ORDER BY td.sort_order, td.created_at")
    List<UserToolConfig> selectEnabledByUserId(String userId);

    @Select("SELECT utc.*, td.code, td.display_name, td.category " +
            "FROM user_tool_configs utc " +
            "LEFT JOIN tool_definitions td ON utc.tool_id = td.id " +
            "WHERE utc.user_id = #{userId} " +
            "ORDER BY td.sort_order, td.created_at")
    List<UserToolConfig> selectByUserId(String userId);

    @Select("SELECT * FROM user_tool_configs WHERE user_id = #{userId} AND tool_id = #{toolId}")
    UserToolConfig selectByUserIdAndToolId(String userId, String toolId);
}