package com.hongshu.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebUser;
import org.apache.catalina.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author hongshu
 */
@Mapper
public interface WebUserMapper extends BaseMapper<WebUser> {

    /**
     * 根据条件分页查询角色数据
     *
     * @param user 会员
     * @return 角色数据集合信息
     */
    List<WebUser> getUserList(WebUser user);

    @Select("select * from web_user where id=#{sellerId}")
    WebUser getUserInfo(Integer sellerId);

    @Select("select user_id from sys_user where sys_user.user_name like concat('%', #{username}, '%')")
    List<Long> selectUserIdsByUsername(String uid);
}
