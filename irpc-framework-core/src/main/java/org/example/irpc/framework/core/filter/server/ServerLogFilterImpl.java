package org.example.irpc.framework.core.filter.server;

import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.filter.IServerFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:58 PM
 */
public class ServerLogFilterImpl implements IServerFilter {
    private static Logger logger = LoggerFactory.getLogger(ServerLogFilterImpl.class);
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        logger.info(rpcInvocation.getAttachments().get("c_app_name") + " do invoke -----> " + rpcInvocation.getTargetServiceName() + "#" + rpcInvocation.getTargetMethod());
    }
}
