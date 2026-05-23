package com.hongshu.server.controller.web;

import com.hongshu.common.enums.Result;
import com.hongshu.common.enums.ResultCodeEnum;
import com.hongshu.common.utils.StringUtils;
import com.hongshu.common.utils.StringUtilss;
import com.hongshu.common.validator.myVaildator.noLogin.NoLoginIntercept;
import com.hongshu.web.auth.AuthContextHolder;
import com.hongshu.web.domain.entity.Product;
import com.hongshu.web.domain.entity.WebOrder;
import com.hongshu.web.domain.vo.GoodsVo;
import com.hongshu.web.mapper.WebProductMapper;
import com.hongshu.web.service.IWebFileService;
import com.hongshu.web.service.IWebProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web/product")
public class WebProductController {
    @Autowired
    private IWebFileService webFileService;
    @Resource
    private IWebProductService webProductService;

    @PostMapping("/upload")
    @NoLoginIntercept
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 调用业务层保存图片
        String picUrl = webFileService.uploadFile(file);

        if (StringUtilss.isEmpty(picUrl)) {
            // 对齐你的 Result 类中的 build 方法或业务错误处理
            return Result.build(ResultCodeEnum.FAIL.getCode(), "图片物理写入失败");
        }

        // 成功返回相对路径: /profile/xxx.jpg
        return Result.ok(picUrl);
    }

    /**
     * 新增商城商品
     * URL: /api/product/add
     */
    @PostMapping("/add")
    public Result<Object> add(@RequestBody Product product) {
        // 从AuthContextHolder获取用户ID（和其他模块一致）
        String currentUid = AuthContextHolder.getUserId();

        if (StringUtils.isEmpty(currentUid)) {
            return Result.fail("未识别到登录用户信息");
        }

        try {
            product.setSellerId(Integer.valueOf(currentUid));
            product.setStatus(1);
            product.setCreateTime(LocalDateTime.now());

            boolean isSaved = webProductService.save(product);

            if (isSaved) {
                return Result.ok();
            } else {
                return Result.fail("数据库写入失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("服务器内部错误：" + e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Product>> list() {
        List<Product> list = webProductService.list();
        return Result.ok(list);
    }

    @GetMapping("/get/{id}")
    public Result<Object> getById(@PathVariable Integer id) {
        GoodsVo goodInfo = webProductService.getById(id);
        if (goodInfo == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok(goodInfo);
    }

    @PostMapping("/buy/{productId}")
    public Result buyProduct(
            @PathVariable Integer productId,
            @RequestParam Integer quantity)
    {return Result.ok(webProductService.addOrder(productId, quantity));}

    /**
     * 2. 确认付款 (更新订单状态)
     */
    @PostMapping("/confirmPay/{orderNo}")
    @Transactional
    public Result confirmPay(@PathVariable String orderNo) {

        return webProductService.confirmPay(orderNo);
    }

    @PostMapping("/offShelf/{productId}")
    public Result offShelf(@PathVariable Integer productId) {
        return Result.ok(webProductService.offShelf(productId));
    }

    @PutMapping("/update")
    public Result update(@RequestBody Product product) {
        return Result.ok(webProductService.updateProductById(product));
    }


}
