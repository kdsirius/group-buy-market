package com.sirius.domain.activity.service.trial;

import com.sirius.domain.activity.model.entity.MarketProductEntity;
import com.sirius.domain.activity.model.entity.TrialBalanceEntity;
import com.sirius.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.sirius.types.design.framework.tree.AbstractStrategyRouter;

/**
 * @author Sirius
 * @description 抽象地拼团营销支撑类
 * @date 2026年09月18日 17:07
 */
public abstract class AbstractGroupBuyMarketSupport extends AbstractStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

}
