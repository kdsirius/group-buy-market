package com.sirius.common.design.framework.link.mode2;

import com.sirius.common.design.framework.link.mode2.chain.BusinessLinkedList;
import com.sirius.common.design.framework.link.mode2.handler.ILogicHandler;

/**
 * @author Sirius
 * @description 链路装配
 * @date 2026年09月23日 21:07
 */
public class LinkArmory<T, D, R> {

    private final BusinessLinkedList<T, D, R> logicLink;

    @SafeVarargs
    public LinkArmory(String name, ILogicHandler<T, D, R>... logicHandlers) {
        logicLink = new BusinessLinkedList<>(name);
        for (ILogicHandler<T, D, R> logicHandler : logicHandlers) {
            logicLink.add(logicHandler);
        }
    }

    public BusinessLinkedList<T, D, R> getLogicLink() {
        return logicLink;
    }
}
