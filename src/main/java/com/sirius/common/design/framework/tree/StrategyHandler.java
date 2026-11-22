package com.sirius.common.design.framework.tree;

/**
 * @author Sirius
 * @description 策略处理器
 * @date 2026年09月18日 16:11
 */
public interface StrategyHandler<T, D, R> {

    /**
     * 受理执行的业务流程。每个业务流程执行时，如果有数据是从前面节点到后面节点要使用的，那么可以填充到 dynamicContext 上下文中
     *
     * @param requestParameter 请求参数
     * @param dynamicContext   动态上下文
     * @return 处理结果
     */
    R apply(T requestParameter, D dynamicContext) throws Exception;

    /**
     * 默认策略
     */
    @SuppressWarnings("all")
    StrategyHandler DEFAULT = (requestParameter, dynamicContext) -> null;
}
