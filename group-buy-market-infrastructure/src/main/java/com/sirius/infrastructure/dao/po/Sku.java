package com.sirius.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Sirius
 * @description 商品信息实体对象
 * @date 2026年09月19日 13:07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sku implements Serializable {

    private static final long serialVersionUID = 1L;

    // 自增ID
    private Integer id;

    // 渠道
    private String source;

    // 来源
    private String channel;

    // 商品ID (varchar类型，映射为String)
    private String goodsId;

    // 商品名称
    private String goodsName;

    // 商品价格 (涉及金额使用BigDecimal)
    private BigDecimal originalPrice;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
