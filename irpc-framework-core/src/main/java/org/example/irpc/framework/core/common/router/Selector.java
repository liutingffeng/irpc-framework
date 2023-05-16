package org.example.irpc.framework.core.common.router;

import org.example.irpc.framework.core.common.ChannelFutureWrapper;

/**
 * @Author liutingfeng
 * @Date 2023/4/17 5:29 PM
 */
public class Selector {
    /**
     * 服务命名
     * eg: com.sise.test.DataService
     */
    private String providerServiceName;

    /**
     * 经过二次筛选之后的future集合
     */
    private ChannelFutureWrapper[] channelFutureWrappers;

    public String getProviderServiceName() {
        return providerServiceName;
    }

    public void setProviderServiceName(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }

    public ChannelFutureWrapper[] getChannelFutureWrappers() {
        return channelFutureWrappers;
    }
}
