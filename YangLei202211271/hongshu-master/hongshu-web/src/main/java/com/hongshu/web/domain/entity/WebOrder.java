package com.hongshu.web.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.io.Serializable;

/**
 * 订单表实体类 (WebOrder)
 * 对应数据库表：web_order
 */
@Data
public class WebOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 ID (自增) */
    private Integer id;

    /** 订单号 (唯一索引) */
    private String orderNo;

    /** 商品 ID */
    private Integer productId;

    /** 买家 ID */
    private Integer buyerId;

    /** 卖家 ID */
    private Integer sellerId;

    /** 支付金额 (BigDecimal 保证精度) */
    private BigDecimal amount;

    /** 支付时间 */
    private Date payTime;

    /** 创建时间 (默认 CURRENT_TIMESTAMP) */
    private Date createTime;

    private int status;

    private String buyerName;
    private String sellerName;
    private Integer count;
}