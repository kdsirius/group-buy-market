package com.sirius.domain.activity.adapter.repository;

import com.sirius.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.sirius.domain.activity.model.valobj.SCSkuActivityVO;
import com.sirius.domain.activity.model.valobj.SkuVO;

/**
 * @author Sirius
 * @description 活动仓储
 * @date 2026年09月19日 12:54
 */
public interface IActivityRepository {

    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId);

    SkuVO querySkuByGoodsId(String goodsId);

    SCSkuActivityVO querySCSkuActivityBySCGoodsId(String source, String channel, String goodsId);

    boolean isTagCrowdRange(String tagId, String userId);
}
