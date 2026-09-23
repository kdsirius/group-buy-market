package com.sirius.types.design.framework.link.mode2.chain;

/**
 * @author Sirius
 * @description 链接口
 * @date 2026年09月23日 16:42
 */
public interface ILink<E> {

    boolean add(E e);

    boolean addFirst(E e);

    boolean addLast(E e);

    boolean remove(Object o);

    E get(int index);

    void printLinkedList();

}
