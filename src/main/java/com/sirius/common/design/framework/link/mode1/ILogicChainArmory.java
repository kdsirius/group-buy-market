package com.sirius.common.design.framework.link.mode1;

/**
 * @author Sirius
 * @description 责任链装配
 * @date 2026年09月23日 15:54
 */
public interface ILogicChainArmory<T, D, R> {

    ILogicLink<T, D, R> next();

    ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next);

}
