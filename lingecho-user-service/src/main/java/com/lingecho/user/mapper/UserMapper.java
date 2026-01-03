package com.lingecho.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lingecho.user.models.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}

