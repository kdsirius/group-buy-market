package com.sirius.service.activity.trial;

import com.sirius.repository.ActivityRepository;
import com.sirius.model.entity.MarketProductEntity;
import com.sirius.model.entity.TrialBalanceEntity;
import com.sirius.service.activity.trial.factory.DefaultActivityStrategyFactory;
import com.sirius.common.design.framework.tree.AbstractMultiThreadStrategyRouter;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @author Sirius
 * @description 抽象地拼团营销支撑类
 * @date 2026年09月18日 17:07
 */
public abstract class AbstractGroupBuyMarketSupport extends AbstractMultiThreadStrategyRouter<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    protected long timeout = 500L;

    @Resource
    protected ActivityRepository repository;

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        //缺省的方法
    }
}
