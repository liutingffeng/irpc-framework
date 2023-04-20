package org.example.irpc.framework.core.filter.client;

import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.filter.IClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.CLIENT_CONFIG;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:26 PM
 */
public class ClientLogFilterImpl implements IClientFilter {
    private static Logger logger = LoggerFactory.getLogger(ClientLogFilterImpl.class);
    @Override
    public void doFilter(List<ChannelFutureWrapper> src, RpcInvocation rpcInvocation) {
        rpcInvocation.getAttachments().put("c_app_name", CLIENT_CONFIG.getApplicationName());
        logger.info(rpcInvocation.getAttachments().get("c_app_name")+" do invoke -----> "+rpcInvocation.getTargetServiceName());
    }
}
