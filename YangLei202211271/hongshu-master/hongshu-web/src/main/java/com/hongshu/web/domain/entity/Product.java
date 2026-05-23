package com.hongshu.web.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("web_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String productName;

    private BigDecimal price; // 必须用 BigDecimal 对应数据库的 decimal

    private Integer stock;

    private String cover;

    private String description;

    private Integer sellerId; // 对应数据库 int

    private Integer status; // 对应数据库 tinyint

    private LocalDateTime createTime; // 对应数据库 datetime
}