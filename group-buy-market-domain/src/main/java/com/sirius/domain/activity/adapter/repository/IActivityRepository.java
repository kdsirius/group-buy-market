package com.sirius.domain.activity.adapter.repository;

import com.sirius.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.sirius.domain.activity.model.valobj.SkuVO;

/**
 * @author Sirius
 * @description 活动仓储
 * @date 2026年09月19日 12:54
 */
public interface IActivityRepository {
    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel);

    SkuVO querySkuByGoodsId(String goodsId);
}
