package com.hongshu.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hongshu.web.domain.entity.WebLikeOrCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author hongshu
 */
@Mapper
public interface WebLikeOrCollectMapper extends BaseMapper<WebLikeOrCollect> {
    @Select("select count(*) from web_like_or_collect where like_or_collection_id=#{id} and uid=#{userId} and type=1")
    Integer getById(@Param("id") String id, @Param("userId") Integer userId);

    @Select("select count(*) from web_like_or_collect where like_or_collection_id= #{id} and type=1")
    Integer getLikeCount(String id);

    @Select("select count(*) from web_like_or_collect where like_or_collection_id= #{id} and type=2")
    Integer getCollectCount(String id);
}
