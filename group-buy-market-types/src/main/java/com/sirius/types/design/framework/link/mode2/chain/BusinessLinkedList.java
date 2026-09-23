package com.sirius.types.design.framework.link.mode2.chain;

import com.sirius.types.design.framework.link.mode2.handler.ILogicHandler;

/**
 * @author Sirius
 * @description 业务链路
 * @date 2026年09月23日 21:00
 */
public class BusinessLinkedList<T, D, R> extends LinkedList<ILogicHandler<T, D, R>> implements ILogicHandler<T, D, R> {

    public BusinessLinkedList(String name) {
        super(name);
    }

    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        Node<ILogicHandler<T, D, R>> current = this.first;
        do {
            ILogicHandler<T, D, R> item = current.item;
            R apply = item.apply(requestParameter, dynamicContext);
            if (null != apply) return apply;

            current = current.next;
        } while (current != null);

        return null;
    }

}
