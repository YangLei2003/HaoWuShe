package com.hongshu.web.service.impl;

import com.hongshu.web.domain.entity.Product;
import com.hongshu.web.mapper.SysProductMapper;
import com.hongshu.web.service.ISysProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysProductServiceImpl implements ISysProductService {

    @Autowired
    private SysProductMapper productMapper;

    @Override
    public List<Product> selectProductList(Product product) {
        // 🚩 这里现在接收的是 Product 对象，直接传给 Mapper
        return productMapper.selectProductList(product);
    }

    @Override
    public Product selectProductById(Long id) {
        return productMapper.selectProductById(id);
    }

    @Override
    public int deleteProductByIds(Long[] ids) {
        // 只取传过来的第一个 ID 进行删除
        if (ids != null && ids.length > 0) {
            return productMapper.deleteProductById(ids[0]);
        }
        return 0;
    }

    @Override
    public int changeStatus(Product product) {
        return productMapper.changeStatus(product);
    }

    @Override
    public int updateProduct(Product product) {
        return productMapper.updateProduct(product);
    }
}