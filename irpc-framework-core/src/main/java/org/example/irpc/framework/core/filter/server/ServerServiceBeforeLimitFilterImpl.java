package org.example.irpc.framework.core.filter.server;

import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.common.ServerServiceSemaphoreWrapper;
import org.example.irpc.framework.core.common.annotations.SPI;
import org.example.irpc.framework.core.common.exception.MaxServiceLimitRequestException;
import org.example.irpc.framework.core.filter.IServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

import static org.example.irpc.framework.core.common.cache.CommonServerCache.SERVER_SERVICE_SEMAPHORE_MAP;

/**
 * @Author liutingfeng
 * @Date 2023/5/15 5:27 PM
 */
@SPI("before")
public class ServerServiceBeforeLimitFilterImpl implements IServerFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerServiceBeforeLimitFilterImpl.class);
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String serviceName = rpcInvocation.getTargetServiceName();
        ServerServiceSemaphoreWrapper serverServiceSemaphoreWrapper = SERVER_SERVICE_SEMAPHORE_MAP.get(serviceName);
        Semaphore semaphore = serverServiceSemaphoreWrapper.getSemaphore();
        // 尝试获取信号量
        boolean tryResult = semaphore.tryAcquire();
        // 如果获取不到信号量，抛出异常
        if (!tryResult) {
            LOGGER.error("[ServerServiceBeforeLimitFilterImpl] {}'s max request is {},reject now", rpcInvocation.getTargetServiceName(), serverServiceSemaphoreWrapper.getMaxNums());
            MaxServiceLimitRequestException iRpcException = new MaxServiceLimitRequestException(rpcInvocation);
            rpcInvocation.setE(iRpcException);
            throw iRpcException;
        }
    }
}
