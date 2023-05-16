package org.example.irpc.framework.core.filter.server;

import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.common.annotations.SPI;
import org.example.irpc.framework.core.common.utils.CommonUtils;
import org.example.irpc.framework.core.filter.IServerFilter;
import org.example.irpc.framework.core.server.ServiceWrapper;

import static org.example.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_SERVICE_WRAPPER_MAP;

/**
 * @Author liutingfeng
 * @Date 2023/4/19 4:59 PM
 */
@SPI("before")
public class ServerTokenFilterImpl implements IServerFilter {
    @Override
    public void doFilter(RpcInvocation rpcInvocation) {
        String token = String.valueOf(rpcInvocation.getAttachments().get("serviceToken"));
        ServiceWrapper serviceWrapper = PROVIDER_SERVICE_WRAPPER_MAP.get(rpcInvocation.getTargetServiceName());
        String matchToken = serviceWrapper.getServiceToken();
        if (CommonUtils.isEmpty(matchToken)) {
            return;
        }
        if (!CommonUtils.isEmpty(token) && token.equals(matchToken)) {
            return;
        }
        throw new RuntimeException("token is " + token + " , verify result is false!");
    }
}
