package com.example.manus.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.manus.persistence.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}