package org.example.irpc.framework.core.common.event.listener;

import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.common.event.IRpcListener;
import org.example.irpc.framework.core.common.event.IRpcNodeChangeEvent;
import org.example.irpc.framework.core.common.router.IRouter;
import org.example.irpc.framework.core.registy.URL;
import org.example.irpc.framework.core.registy.zookeeper.ProviderNodeInfo;

import java.util.List;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static org.example.irpc.framework.core.common.cache.CommonClientCache.IROUTER;

/**
 * @Author liutingfeng
 * @Date 2023/4/17 7:49 PM
 */
public class ProviderNodeDataChangeListener implements IRpcListener<IRpcNodeChangeEvent> {
    @Override
    public void callBack(IRpcNodeChangeEvent iRpcNodeChangeEvent) {
        ProviderNodeInfo providerNodeInfo = (ProviderNodeInfo) iRpcNodeChangeEvent.getData();
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerNodeInfo.getServiceName());
        for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
            String address = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
            if (address.equals(providerNodeInfo.getAddress())) {
                // 修改权重
                channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
                URL url = new URL();
                url.setServiceName(providerNodeInfo.getServiceName());
                // 更新权重 这里对应了文章顶部的RandomRouterImpl类
                IROUTER.updateWeight(url);
                break;
            }
        }
    }
}
