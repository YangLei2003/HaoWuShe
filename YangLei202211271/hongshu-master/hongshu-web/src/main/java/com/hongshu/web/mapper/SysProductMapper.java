package com.hongshu.web.mapper;

import com.hongshu.web.domain.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysProductMapper {

    @Select("select id, " +
            "product_name as productName, " + // 重点：加别名
            "price, stock, cover, description, " +
            "seller_id as sellerId, " +       // 重点：加别名
            "status, " +
            "create_time as createTime " +    // 重点：加别名
            "from web_product " +
            "where (product_name like concat('%', #{productName}, '%') or #{productName} is null) " +
            "order by create_time desc")
    List<Product> selectProductList(Product product);

    @Select("select id, product_name as productName, price, stock, cover, description, " +
            "seller_id as sellerId, status, create_time as createTime " +
            "from web_product where id = #{id}")
    Product selectProductById(Long id);

    @Update("update web_product set status = #{status} where id = #{id}")
    int changeStatus(Product product);

    /**
     * 删除单个商品
     */
    @Delete("delete from web_product where id = #{id}")
    int deleteProductById(Long id);

    @Update("update web_product set " +
            "product_name = #{productName}, " +
            "price = #{price}, " +
            "stock = #{stock}, " +
            "cover = #{cover}, " +
            "description = #{description}, " +
            "seller_id = #{sellerId}, " +
            "status = #{status} " +
            "where id = #{id}")
    int updateProduct(Product product);
}