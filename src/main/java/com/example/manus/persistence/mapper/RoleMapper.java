package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}