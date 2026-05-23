package com.hongshu.web.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hongshu.web.domain.entity.Product;
import com.hongshu.web.domain.vo.NoteSearchVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WebProductMapper {

    @Insert("INSERT INTO web_product(product_name, price, stock, cover, description, seller_id, status, create_time) " +
            "VALUES(#{productName}, #{price}, #{stock}, #{cover}, #{description}, #{sellerId}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    public int insert(Product product);

    @Select("SELECT * FROM web_product WHERE status = 1 ORDER BY create_time DESC")
    List<Product> selectProductList();

    @Select("select * from web_product where status=1 and id=#{id}")
    Product getById(Integer id);

    @Update("UPDATE web_product SET stock = stock - #{stock} WHERE id = #{id} AND stock > 0")
    Boolean reduceStockById(@Param("id") Integer id, @Param("stock") Integer stock);

    @Delete("DELETE FROM web_product WHERE id = #{productId}")
    Boolean deleteById(Integer productId);

    @Update("UPDATE web_product SET product_name = #{productName}, price = #{price}, stock = #{stock}, cover = #{cover}, description = #{description} WHERE id = #{id}")
    int updateProductById(Product product);

    @Select("SELECT id, product_name AS title, cover, price, stock FROM web_product WHERE status = 1 AND seller_id = #{userId} ORDER BY create_time DESC")
    Page<Product> getByUserId(Page<?> page, @Param("userId") String userId);
}
