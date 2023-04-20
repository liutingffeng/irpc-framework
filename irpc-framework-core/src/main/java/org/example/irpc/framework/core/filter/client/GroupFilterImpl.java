package org.example.irpc.framework.core.filter.client;

import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.common.utils.CommonUtils;
import org.example.irpc.framework.core.filter.IClientFilter;

import java.util.List;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:29 PM
 */
public class GroupFilterImpl implements IClientFilter {
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        String group = String.valueOf(rpcInvocation.getAttachments().get("group"));
        for (ChannelFutureWrapper channelFutureWrapper : src) {
            if (!channelFutureWrapper.getGroup().equals(group)) {
                src.remove(channelFutureWrapper);
            }
        }
        if (CommonUtils.isEmptyList(src)) {
            throw new RuntimeException("no provider match for group " + group);
        }
    }
}
