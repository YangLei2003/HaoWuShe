package com.hongshu.web.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository("systemUserMapper")
public interface SysUserMapper {

    @Select("select user_name from sys_user where user_id=1")
    String getUserName();
}
