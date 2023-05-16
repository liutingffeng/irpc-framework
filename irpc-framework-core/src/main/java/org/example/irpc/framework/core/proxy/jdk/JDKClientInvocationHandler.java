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
        rpcInvocation.setRetry(rpcReferenceWrapper.getRetry());
        // 这里就是将请求的参数放入到发送队列中
        SEND_QUEUE.add(rpcInvocation);
        // 既然是异步请求，就没有必要再在RESP_MAP中判断是否有响应结果了
        if (rpcReferenceWrapper.isAsync()) {
            return null;
        }
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
        long beginTime = System.currentTimeMillis();
        //客户端请求超时的一个判断依据
        int retryTimes = 0;
        while (System.currentTimeMillis() - beginTime < timeOut || rpcInvocation.getRetry() > 0) {
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object != null && object instanceof RpcInvocation) {
                RpcInvocation rpcInvocationResp = (RpcInvocation) object;
                // 正常结果
                if (rpcInvocationResp.getRetry() == 0 || (rpcInvocationResp.getRetry() != 0 && rpcInvocationResp.getE() == null)) {
                    RESP_MAP.remove(rpcInvocation.getUuid());
                    return rpcInvocationResp.getResponse();
                } else if (rpcInvocationResp.getE() != null) {
                    if (rpcInvocationResp.getRetry() == 0) {
                        RESP_MAP.remove(rpcInvocation.getUuid());
                        return rpcInvocationResp.getResponse();
                    }
                }
            }
            if (OBJECT.equals(object)) {
                //如果是因为超时的情况，才会触发重试规则，否则重试机制不生效
                if (System.currentTimeMillis() - beginTime > timeOut) {
                    retryTimes++;
                    //重新请求
                    rpcInvocation.setResponse(null);
                    //每次重试之后都会将retry值扣减1
                    rpcInvocation.setRetry(rpcInvocation.getRetry() - 1);
                    RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);
                    SEND_QUEUE.add(rpcInvocation);
                }
            }
        }
        //修改异常信息
        RESP_MAP.remove(rpcInvocation.getUuid());
        throw new TimeoutException("Wait for response from server on client " + timeOut + "ms,retry times is " + retryTimes + ",service's name is " + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
