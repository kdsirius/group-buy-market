package com.sirius.service.activity.discount.impl;

import com.sirius.model.value.GroupBuyActivityDiscountVO;
import com.sirius.service.activity.discount.AbstractDiscountCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * @author Sirius
 * @description N元购计算
 * @date 2026年09月19日 15:57
 */
@Slf4j
@Service("N")
public class NCalculateService extends AbstractDiscountCalculateService {
    @Override
    protected BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        log.info("优惠策略折扣计算:{}", groupBuyDiscount.getDiscountType().getCode());

        // 折扣表达式 - 直接为优惠后的金额
        String marketExpr = groupBuyDiscount.getMarketExpr();
        // N元购
        return new BigDecimal(marketExpr);
    }
}
