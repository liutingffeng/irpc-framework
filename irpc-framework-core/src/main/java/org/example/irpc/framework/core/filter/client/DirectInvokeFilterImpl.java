package org.example.irpc.framework.core.filter.client;

import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.common.utils.CommonUtils;
import org.example.irpc.framework.core.filter.IClientFilter;

import java.util.Iterator;
import java.util.List;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:55 PM
 */
public class DirectInvokeFilterImpl implements IClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String url = (String) rpcInvocation.getAttachments().get("url");
        if (CommonUtils.isEmpty(url)) {
            return;
        }
        Iterator<ChannelFutureWrapper> channelFutureWrapperIterator = src.iterator();
        while (channelFutureWrapperIterator.hasNext()) {
            ChannelFutureWrapper channelFutureWrapper = channelFutureWrapperIterator.next();
            if (!(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort()).equals(url)) {
                channelFutureWrapperIterator.remove();
            }
        }
        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("no match provider url for "+ url);
        }
    }
}
