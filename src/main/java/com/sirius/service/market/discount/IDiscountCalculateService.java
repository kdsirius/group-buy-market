package com.sirius.service.market.discount;

import com.sirius.model.vo.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

/**
 * @author Sirius
 * @description 折扣计算服务接口
 * @date 2026年09月19日 15:56
 */
public interface IDiscountCalculateService {
    /**
     * 折扣计算
     *
     * @param userId           用户ID
     * @param originalPrice    商品原始价格
     * @param groupBuyDiscount 折扣计划配置
     * @return 商品优惠价格
     */
    BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
