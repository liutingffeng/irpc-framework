package org.example.irpc.framework.core.filter.server;

import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.filter.IFilter;
import org.example.irpc.framework.core.filter.IServerFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:20 PM
 */
public class ServerFilterChain {

    private static List<IServerFilter> iServerFilters = new ArrayList<>();

    public void addServerFilter(IServerFilter iServerFilter) {
        iServerFilters.add(iServerFilter);
    }

    public void doFilter(RpcInvocation rpcInvocation) {
        for (IServerFilter filter: iServerFilters) {
            filter.doFilter(rpcInvocation);
        }
    }
}
