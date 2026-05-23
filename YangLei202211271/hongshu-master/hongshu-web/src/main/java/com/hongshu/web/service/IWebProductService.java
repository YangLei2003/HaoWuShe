package com.hongshu.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hongshu.common.enums.Result;
import com.hongshu.web.domain.entity.Product;
import com.hongshu.web.domain.entity.WebOrder;
import com.hongshu.web.domain.vo.GoodsVo;

import java.util.List;

public interface IWebProductService{
    boolean save(Product product);

    List<Product> list();

    GoodsVo getById(Integer id);

    WebOrder addOrder(Integer id,Integer quantity);

    Result confirmPay(String orderNo);

    Result offShelf(Integer productId);

    Result updateProductById(Product product);
}
