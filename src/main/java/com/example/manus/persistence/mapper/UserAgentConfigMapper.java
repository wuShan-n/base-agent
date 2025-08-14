package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.UserAgentConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserAgentConfigMapper extends BaseMapper<UserAgentConfig> {

    @Select("SELECT uac.*, ad.name as agent_name, ad.display_name, ad.category " +
            "FROM user_agent_configs uac " +
            "INNER JOIN agent_definitions ad ON uac.agent_id = ad.id " +
            "WHERE uac.user_id = #{userId} AND uac.enabled = TRUE AND ad.status = 1 " +
            "ORDER BY uac.is_favorite DESC, uac.usage_count DESC")
    List<UserAgentConfig> selectEnabledByUserId(@Param("userId") String userId);

    @Select("SELECT uac.* FROM user_agent_configs uac " +
            "INNER JOIN agent_definitions ad ON uac.agent_id = ad.id " +
            "WHERE uac.user_id = #{userId} AND uac.is_favorite = TRUE AND uac.enabled = TRUE AND ad.status = 1 " +
            "ORDER BY uac.usage_count DESC")
    List<UserAgentConfig> selectFavoritesByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM user_agent_configs WHERE user_id = #{userId} AND agent_id = #{agentId}")
    UserAgentConfig selectByUserAndAgent(@Param("userId") String userId, @Param("agentId") String agentId);

    @Select("SELECT uac.*, ad.name as agent_name, ad.display_name, ad.category " +
            "FROM user_agent_configs uac " +
            "INNER JOIN agent_definitions ad ON uac.agent_id = ad.id " +
            "WHERE uac.user_id = #{userId} AND ad.category = #{category} AND uac.enabled = TRUE AND ad.status = 1")
    List<UserAgentConfig> selectByUserAndCategory(@Param("userId") String userId, @Param("category") String category);

    @Update("UPDATE user_agent_configs SET usage_count = usage_count + 1, last_used_at = NOW() " +
            "WHERE user_id = #{userId} AND agent_id = #{agentId}")
    Integer incrementUsageCount(@Param("userId") String userId, @Param("agentId") String agentId);

    @Select("SELECT COUNT(*) FROM user_agent_configs WHERE user_id = #{userId} AND enabled = TRUE")
    Integer countEnabledByUserId(@Param("userId") String userId);

    @Select("SELECT COUNT(*) FROM user_agent_configs WHERE user_id = #{userId} AND is_favorite = TRUE")
    Integer countFavoritesByUserId(@Param("userId") String userId);
}