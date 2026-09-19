package com.sirius.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Sirius
 * @description 商品信息
 * @date 2026年09月19日 10:31
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuVO {
    /**
     * 商品ID
     */
    private String goodsId;
    /**
     * 商品名称
     */
    private String goodsName;
    /**
     * 原始价格
     */
    private BigDecimal originalPrice;

}
