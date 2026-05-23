package com.hongshu.web.mapper;

import com.hongshu.web.domain.entity.WebOrder;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface WebOrderMapper {

    @Insert("insert into web_order(order_no, product_id, buyer_id, seller_id, amount, count,pay_time, create_time, status) values(#{orderNo}, #{productId}, #{buyerId}, #{sellerId},#{amount},#{count}, #{payTime}, #{createTime}, #{status})")
    void insert(WebOrder order);

    @Select("select * from web_order where order_no = #{orderNo}")
    WebOrder selectByOrderNo(String orderNo);

    @Update("UPDATE web_order SET status = 1, pay_time = NOW() WHERE order_no = #{orderNo}")
    int updateStatus(String orderNo);

    @Select("select * from web_order order by create_time desc limit #{offset}, #{pageSize}")
    List<WebOrder> selectList(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    @Select("select count(*) from web_order")
    int selectCount();

    @Delete("delete from web_order where id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Update("update web_order set order_no = #{orderNo}, product_id = #{productId}, buyer_id = #{buyerId}, seller_id = #{sellerId}, amount = #{amount}, pay_time = #{payTime}, status = #{status} where id = #{id}")
    int update(WebOrder webOrder);
}