package org.example.irpc.framework.core.proxy;

import org.example.irpc.framework.core.client.RpcReferenceWrapper;

public interface ProxyFactory {
    <T> T getProxy(final RpcReferenceWrapper rpcReferenceWrapper) throws Throwable;
}
