package com.sirius.common.design.framework.link.mode1;

/**
 * @author Sirius
 * @description 略规则责任链接口
 * @date 2026年09月23日 15:56
 */
public interface ILogicLink<T, D, R> extends ILogicChainArmory<T, D, R> {

    R apply(T requestParameter, D dynamicContext) throws Exception;

}
