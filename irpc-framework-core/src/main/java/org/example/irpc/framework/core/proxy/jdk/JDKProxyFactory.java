package org.example.irpc.framework.core.proxy.jdk;

import org.example.irpc.framework.core.client.RpcReferenceWrapper;
import org.example.irpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

public class JDKProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(RpcReferenceWrapper rpcReferenceWrapper) throws Throwable {
        return (T) Proxy.newProxyInstance(rpcReferenceWrapper.getAimClass().getClassLoader(), new Class[]{rpcReferenceWrapper.getAimClass()},
                new JDKClientInvocationHandler(rpcReferenceWrapper));
    }
}
