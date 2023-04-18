package org.example.irpc.framework.core.common.router;


import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.registy.URL;

public interface IRouter {


    /**
     * 刷新路由数组
     *
     * @param selector
     */
    void refreshRouterArr(Selector selector);

    /**
     * 获取到请求到连接通道
     *
     * @return
     */
    ChannelFutureWrapper select(Selector selector);

    /**
     * 更新权重信息
     *
     * @param url
     */
    void updateWeight(URL url);
}
