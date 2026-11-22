package com.sirius.common.design.framework.tree;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Sirius
 * @description 策略路由抽象类
 * @date 2026年09月18日 16:21
 */
@Setter
@Getter
public abstract class AbstractStrategyRouter<T, D, R> implements StrategyHandler<T, D, R>, StrategyMapper<T, D, R> {

    @SuppressWarnings("all")
    protected StrategyHandler<T, D, R> defaultStrategyHandler = StrategyHandler.DEFAULT;

    public R router(T requestParameter, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> strategyHandler = get(requestParameter, dynamicContext);
        if (strategyHandler != null) return strategyHandler.apply(requestParameter, dynamicContext);
        return defaultStrategyHandler.apply(requestParameter, dynamicContext);
    }
}
