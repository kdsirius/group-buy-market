package com.sirius.domain.activity.service.trial.node;

import com.sirius.domain.activity.model.entity.MarketProductEntity;
import com.sirius.domain.activity.model.entity.TrialBalanceEntity;
import com.sirius.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.sirius.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.sirius.types.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 开关节点
 * @date 2026年09月18日 17:06
 */
@Slf4j
@Service
public class SwitchNode extends AbstractGroupBuyMarketSupport {
    @Resource
    private MarketNode marketNode;

    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return marketNode;
    }
}
