package com.hongshu.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Author hongshu
 */
@Mapper
public interface WebCommentMapper extends BaseMapper<WebComment> {
    @Select("select id from web_comment where id=#{id}")
    Integer exist(String id);
}
