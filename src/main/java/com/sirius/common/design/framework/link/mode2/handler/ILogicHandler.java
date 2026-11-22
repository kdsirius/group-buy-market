package com.sirius.common.design.framework.link.mode2.handler;

/**
 * @author Sirius
 * @description 逻辑处理器
 * @date 2026年09月23日 20:52
 */
public interface ILogicHandler<T, D, R> {

    default R next(T requestParameter, D dynamicContext) {
        return null;
    }

    R apply(T requestParameter, D dynamicContext) throws Exception;
}
