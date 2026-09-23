package com.sirius.types.design.framework.link.mode1;

/**
 * @author Sirius
 * @description 抽象类
 * @date 2026年09月23日 16:01
 */
public abstract class AbstractLogicLink<T, D, R> implements ILogicLink<T, D, R> {

    private ILogicLink<T, D, R> next;

    @Override
    public ILogicLink<T, D, R> appendNext(ILogicLink<T, D, R> next) {
        this.next = next;
        return next;
    }

    @Override
    public ILogicLink<T, D, R> next() {
        return next;
    }

    protected R next(T requestParameter, D dynamicContext) throws Exception {
        return next.apply(requestParameter, dynamicContext);
    }
}
