package org.example.irpc.framework.core.filter.client;

import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.filter.IClientFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:23 PM
 */
public class ClientFilterChain {

    private static List<IClientFilter> iClientFilters = new ArrayList<>();

    public void addClientFilter(IClientFilter iClientFilter) {
        iClientFilters.add(iClientFilter);
    }

    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        for (IClientFilter filter : iClientFilters) {
            filter.doFilter(src, rpcInvocation);
        }
    }
}
