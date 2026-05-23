package com.hongshu.web.service;

import com.hongshu.web.domain.entity.Product;

import java.util.List;

public interface ISysProductService {
    /**
     * 查询商品列表
     */
    List<Product> selectProductList(Product product);

    /**
     * 根据ID查询商品
     */
    Product selectProductById(Long id);

    /**
     * 批量删除商品
     */
    int deleteProductByIds(Long[] ids);

    /**
     * 修改商品状态
     */
    int changeStatus(Product product);

    int updateProduct(Product product);
}