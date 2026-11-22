package com.sirius.common.design.framework.tree;

/**
 * @author Sirius
 * @description 策略映射器
 * @date 2026年09月18日 16:10
 */
public interface StrategyMapper<T, D, R> {

    /**
     * 用于获取每一个要执行的节点，相当于一个流程走完进入到下一个流程的过程
     *
     * @param requestParameter 入参
     * @param dynamicContext   动态上下文
     * @return 策略处理器
     * @throws Exception 异常
     */
    StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception;
}
