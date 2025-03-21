package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.sky.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {

}
