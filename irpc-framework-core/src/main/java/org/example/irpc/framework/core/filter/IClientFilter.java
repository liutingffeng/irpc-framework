package org.example.irpc.framework.core.filter;

import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;

import java.util.List;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:15 PM
 */
public interface IClientFilter extends IFilter{

    /**
     * 执行过滤链
     * @param src
     * @param rpcInvocation
     */
    void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation);
}
