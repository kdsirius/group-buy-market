package com.sirius.domain.activity.service;

import com.sirius.domain.activity.model.entity.MarketProductEntity;
import com.sirius.domain.activity.model.entity.TrialBalanceEntity;

/**
 * @author Sirius
 * @description 首页营销服务接口
 * @date 2026年09月18日 17:04
 */
public interface IIndexGroupBuyMarketService {
    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception;
}
