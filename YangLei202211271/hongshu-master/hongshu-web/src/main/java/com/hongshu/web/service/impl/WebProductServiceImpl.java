package com.hongshu.web.service.impl;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.enums.Result;
import com.hongshu.web.auth.AuthContextHolder;
import com.hongshu.web.domain.entity.Product;
import com.hongshu.web.domain.entity.RedisData;
import com.hongshu.web.domain.entity.WebOrder;
import com.hongshu.web.domain.entity.WebUser;
import com.hongshu.web.domain.vo.GoodsVo;
import com.hongshu.web.mapper.WebOrderMapper;
import com.hongshu.web.mapper.WebProductMapper;
import com.hongshu.web.mapper.WebUserMapper;
import com.hongshu.web.service.IWebProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class WebProductServiceImpl implements IWebProductService {
    @Autowired
    private WebProductMapper productMapper;

    @Autowired
    private WebUserMapper userMapper;

    @Autowired
    private WebOrderMapper orderMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public boolean save(Product product) {
        int result = productMapper.insert(product);

        if (result > 0) {
            // 统一用 RedisData 格式
            RedisData redisData = new RedisData();
            redisData.setData(product);
            redisData.setExpireTime(LocalDateTime.now().plusHours(24));
            String productInfo = JSONUtil.toJsonStr(redisData);
            stringRedisTemplate.opsForValue().set("product:" + product.getId(), productInfo);
        }

        return result > 0;
    }

    /**
     * 从Redis进行数据读取
     *
     * @return
     */
    @Override
    public List<Product> list() {
        Set<String> keys = stringRedisTemplate.keys("product:*");

        if (keys == null || keys.isEmpty()) {
            return productMapper.selectProductList();
        }

        List<String> jsonList = stringRedisTemplate.opsForValue().multiGet(keys);
        List<Product> productList = new ArrayList<>();

        for (String json : jsonList) {
            if (json != null) {
                RedisData redisData = JSONUtil.toBean(json, RedisData.class);
                String dataJson = JSONUtil.toJsonStr(redisData.getData());
                Product product = JSONUtil.toBean(dataJson, Product.class);
                productList.add(product);
            }
        }
        return productList;
    }

    @Override
    public GoodsVo getById(Integer id) {
        GoodsVo goods = new GoodsVo();
        Product productInfo = productMapper.getById(id);
        BeanUtils.copyProperties(productInfo, goods);
        WebUser userInfo = userMapper.getUserInfo(productInfo.getSellerId());
        goods.setSellerName(userInfo.getUsername());
        goods.setSellerAvatar(userInfo.getAvatar());
        return goods;
    }


    /**
     * 创建订单
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务，报错自动回滚
    public WebOrder addOrder(Integer id, Integer quantity) {
        Product productBuyed = productMapper.getById(id);

        WebOrder order = new WebOrder();
        order.setOrderNo("order_" + System.currentTimeMillis());
        order.setSellerId(productBuyed.getSellerId());
        order.setProductId(id);
        order.setAmount(productBuyed.getPrice());
        order.setPayTime(new Date());
        order.setCount(quantity);
        order.setCreateTime(new Date());
        order.setBuyerId(Integer.valueOf(AuthContextHolder.getUserId()));
        order.setStatus(0);


        orderMapper.insert(order);

        return Result.ok(order).getData();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result confirmPay(String orderNo) {
        WebOrder order = orderMapper.selectByOrderNo(orderNo);
        //先在Redis中扣减库存,主要是可以一定程度上防止超卖,维护数据库性能
        Integer count = order.getCount();

        //先查库存
        String now_stock = stringRedisTemplate.opsForValue().get("stockOfProduct:stock:" + order.getProductId());
        int IntNowStock = Integer.parseInt(now_stock);
        //如果库存足够
        if (IntNowStock > 0 && IntNowStock >= count) {
            //执行删减库存逻辑 
            //先删库存信息key
            stringRedisTemplate.delete("stockOfProduct:stock:" + order.getProductId());
            Integer newStock = IntNowStock - count;
            //再重新写入更新库存的key
            stringRedisTemplate.opsForValue().set("stockOfProduct:stock:" + order.getProductId(), newStock.toString());
            //去数据库减少库存
            productMapper.reduceStockById(order.getProductId(), order.getCount());
            //更新订单状态
            orderMapper.updateStatus(orderNo);
            //删除商品信息key
            stringRedisTemplate.delete("product:" + order.getProductId());
            //获取商品信息
            Product productId = productMapper.getById(order.getProductId());

            RedisData redisData = new RedisData();
            redisData.setData(productId);
            redisData.setExpireTime(LocalDateTime.now().plusHours(24));
            String productStr = JSONUtil.toJsonStr(redisData);

            stringRedisTemplate.opsForValue().set("product:" + order.getProductId(), productStr);
            return Result.ok();
        } else {
            return Result.fail("商品卖完啦!手速太慢了哦");
        }
    }

    @Override
    public Result offShelf(Integer productId) {
        //从数据库删除商品
        Boolean success = productMapper.deleteById(productId);
        if (success) {
            //从Redis中删除商品
            stringRedisTemplate.delete("product:" + productId);
            stringRedisTemplate.delete("stockOfProduct:stock:" + productId);
        } else {
            return Result.fail("商品不存在");
        }
        return Result.ok();
    }

    @Override
    public Result updateProductById(Product product) {
        Result.ok(productMapper.updateProductById(product));
        //先删除再重新添加缓存来做到更新
        stringRedisTemplate.delete("product:" + product.getId());
        stringRedisTemplate.delete("stockOfProduct:stock:" + product.getId());
        RedisData redisData = new RedisData();
        redisData.setData(product);
        redisData.setExpireTime(LocalDateTime.now().plusHours(24));
        String jsonStr = JSONUtil.toJsonStr(redisData);
        stringRedisTemplate.opsForValue().set("product:" + product.getId(), jsonStr);
        stringRedisTemplate.opsForValue().set("stockOfProduct:stock:" + product.getId(), product.getStock().toString());
        return Result.ok();
    }

}
