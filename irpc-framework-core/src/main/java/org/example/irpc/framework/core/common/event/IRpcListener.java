package org.example.irpc.framework.core.common.event;

public interface IRpcListener<T> {
    void callBack(T t);
}
