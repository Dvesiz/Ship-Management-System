package com.dhy.shipmanagebackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dhy.shipmanagebackend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from users where email = #{email}")
    User findByEmail(String email);


}
