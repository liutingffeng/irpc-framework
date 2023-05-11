package org.example.irpc.framework.core.proxy.jdk;

import org.example.irpc.framework.core.client.RpcReferenceWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static org.example.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;
import static org.example.irpc.framework.core.common.constants.RpcConstants.DEFAULT_TIMEOUT;

public class JDKClientInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();
    private RpcReferenceWrapper rpcReferenceWrapper;
    private Long timeOut = Long.valueOf(DEFAULT_TIMEOUT);
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
        // 这里就是将请求的参数放入到发送队列中
        SEND_QUEUE.add(rpcInvocation);
        // 既然是异步请求，就没有必要再在RESP_MAP中判断是否有响应结果了
        if (rpcReferenceWrapper.isAsync()) {
            return null;
        }
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        long beginTime = System.currentTimeMillis();
        //客户端请求超时的一个判断依据
        while (System.currentTimeMillis() - beginTime < timeOut) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation) {
                return ((RpcInvocation)object).getResponse();
            }
        }
        //修改异常信息
        throw new TimeoutException("Wait for response from server on client " + timeOut + "ms,Service's name is " + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
