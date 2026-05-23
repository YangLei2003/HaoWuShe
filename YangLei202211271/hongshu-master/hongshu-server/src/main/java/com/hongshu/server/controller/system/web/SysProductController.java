package com.hongshu.server.controller.system.web;

import com.hongshu.common.annotation.Log;
import com.hongshu.common.core.controller.BaseController;
import com.hongshu.common.core.domain.AjaxResult;
import com.hongshu.common.core.page.TableDataInfo;
import com.hongshu.common.enums.BusinessType;
import com.hongshu.web.domain.entity.Product;
import com.hongshu.web.service.ISysProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 商品管理操作处理
 *
 * @Author hongshu
 */
@RestController
@RequestMapping("/product")
public class SysProductController extends BaseController {

    @Autowired
    private ISysProductService productService;

    /**
     * 获取商品列表
     */
    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public TableDataInfo list(Product product) { // 🚩 改为直接使用 Product 接收
        this.startPage();
        // 🚩 传参改为 product 实体
        List<Product> productList = productService.selectProductList(product);
        return getDataTable(productList);
    }

    /**
     * 根据商品ID获取详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(productService.selectProductById(id));
    }

    /**
     * 删除商品
     */
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(productService.deleteProductByIds(ids));
    }

    /**
     * 上架/下架商品
     */
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody Product product) {
        return toAjax(productService.changeStatus(product));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody Product product){
        return toAjax(productService.updateProduct(product));
    }
}