package org.example.irpc.framework.core.proxy.jdk;

import org.example.irpc.framework.core.client.RpcReferenceWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static org.example.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

public class JDKClientInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();
    private RpcReferenceWrapper rpcReferenceWrapper;
    public JDKClientInvocationHandler(RpcReferenceWrapper rpcReferenceWrapper) {
        this.rpcReferenceWrapper = rpcReferenceWrapper;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(rpcReferenceWrapper.getAimClass().getName());
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        rpcInvocation.setAttachments(rpcReferenceWrapper.getAttatchments());
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        // 这里就是将请求的参数放入到发送队列中
        SEND_QUEUE.add(rpcInvocation);
        long beginTime = System.currentTimeMillis();
        //客户端请求超时的一个判断依据
        while (System.currentTimeMillis() - beginTime < 3*1000) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                return ((RpcInvocation)object).getResponse();
            }
        }
//        throw new TimeoutException("client wait server's response timeout!");
        return null;
    }
}
