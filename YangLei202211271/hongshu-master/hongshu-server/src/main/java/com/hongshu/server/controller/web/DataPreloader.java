package com.hongshu.server.controller.web;

import cn.hutool.json.JSONUtil;
import com.hongshu.common.utils.JsonUtils;
import com.hongshu.web.domain.entity.Product;
import com.hongshu.web.domain.entity.RedisData;
import com.hongshu.web.service.IWebProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataPreloader implements CommandLineRunner {
    @Autowired
    private IWebProductService webProductService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void run(String... args) {
        // 1. 从 DB 查出热点商品
        List<Product> list = webProductService.list();
        // 2. 批量写入 Redis
        String key = "product:";
        String stockKey = "stockOfProduct:stock:";
        stringRedisTemplate.delete(key);
        for (Product product : list) {
            if (product == null) {
                continue;
            }

            if (product.getId() == null) {
                continue;
            }
            //用中间实体类来存储数据,主要是为了TTL
            RedisData redisData=new RedisData();
            redisData.setData( product);
            redisData.setExpireTime(LocalDateTime.now().plusHours(24));
            String productInfo= JSONUtil.toJsonStr(redisData);
            //存储商品数据到Redis
            stringRedisTemplate.opsForValue().set(key + product.getId(), productInfo);
            //存储商品库存数据到Redis
            Integer stock = product.getStock();
            if(stock==null){
                stock=0;
            }
            stringRedisTemplate.opsForValue().set(stockKey + product.getId(), stock.toString());
        }
        System.out.println("商品数据预热完毕");
    }
}