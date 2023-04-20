package org.example.irpc.framework.core.filter;

import org.example.irpc.framework.core.common.RpcInvocation;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:19 PM
 */
public interface IServerFilter extends IFilter{

    /**
     * 执行核心过滤逻辑
     *
     * @param rpcInvocation
     */
    void doFilter(RpcInvocation rpcInvocation);
}
