package org.example.irpc.framework.core.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import org.example.irpc.framework.core.common.ChannelFutureWrapper;
import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.common.router.Selector;
import org.example.irpc.framework.core.common.utils.CommonUtils;
import org.example.irpc.framework.core.registy.URL;
import org.example.irpc.framework.core.registy.zookeeper.ProviderNodeInfo;

import java.util.*;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.*;

public class ConnectionHandler {

    /**
     * 核心的连接处理器
     * 专门用于负责和服务端构建连接通信
     */
    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }

    /**
     * 构建单个连接通道 元操作，既要处理连接，还要统一将连接进行内存存储管理
     *
     * @param providerIp
     * @return
     * @throws InterruptedException
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException {
        if (bootstrap == null) {
            throw new RuntimeException("bootstrap can not be null");
        }
        //格式错误类型的信息
        if (!providerIp.contains(":")) {
            return;
        }
        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        Integer port = Integer.parseInt(providerAddress[1]);
        //到底这个channelFuture里面是什么
        ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
        String providerURLInfo = URL_MAP.get(providerServiceName).get(providerIp);
        ProviderNodeInfo providerNodeInfo = URL.buildURLFromUrlStr(providerURLInfo);
        //todo 缺少一个将url进行转换的组件
        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        channelFutureWrapper.setWeight(providerNodeInfo.getWeight());
        channelFutureWrapper.setGroup(providerNodeInfo.getGroup());
        SERVER_ADDRESS.add(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            channelFutureWrappers = new ArrayList<>();
        }
        channelFutureWrappers.add(channelFutureWrapper);
        //例如com.sise.test.UserService会被放入到一个Map集合中，key是服务的名字，value是对应的channel通道的List集合
        CONNECT_MAP.put(providerServiceName, channelFutureWrappers);
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
        IROUTER.refreshRouterArr(selector);
    }

    /**
     * 构建ChannelFuture
     * @param ip
     * @param port
     * @return
     * @throws InterruptedException
     */
    public static ChannelFuture creatureChannelFuture(String ip, Integer port) {
        ChannelFuture channelFuture = bootstrap.connect(ip, port);
        return channelFuture;
    }

    /**
     * 断开连接
     *
     * @param providerServiceName
     * @param providerIp
     */
    public static void disConnect(String providerServiceName, String providerIp) {
        SERVER_ADDRESS.remove(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isNotEmptyList(channelFutureWrappers)) {
            Iterator<ChannelFutureWrapper> iterator = channelFutureWrappers.iterator();
            while (iterator.hasNext()) {
                ChannelFutureWrapper channelFutureWrapper = iterator.next();
                if (providerIp.equals(channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 默认走随机策略获取ChannelFuture
     *
     * @param rpcInvocation
     * @return
     */
    public static ChannelFuture getChannelFuture(RpcInvocation rpcInvocation) {
        String providerServiceName = rpcInvocation.getTargetServiceName();
        List<ChannelFutureWrapper> channelFutureWrappers = Arrays.asList(SERVICE_ROUTER_MAP.get(providerServiceName));
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
        CLIENT_FILTER_CHAIN.doFilter(channelFutureWrappers, rpcInvocation);
        Selector selector = new Selector();
        selector.setProviderServiceName(providerServiceName);
//        ChannelFuture channelFuture = channelFutureWrappers.get(new Random().nextInt(channelFutureWrappers.size())).getChannelFuture();
        ChannelFuture channelFuture = IROUTER.select(selector).getChannelFuture();
        return channelFuture;
    }
}
