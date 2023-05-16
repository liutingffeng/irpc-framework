package org.example.irpc.framework.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.example.irpc.framework.core.common.RpcDecoder;
import org.example.irpc.framework.core.common.RpcEncoder;
import org.example.irpc.framework.core.common.config.PropertiesBootstrap;
import org.example.irpc.framework.core.common.config.ServerConfig;
import org.example.irpc.framework.core.common.event.IRpcListenerLoader;
import org.example.irpc.framework.core.common.utils.CommonUtils;
import org.example.irpc.framework.core.filter.IServerFilter;
import org.example.irpc.framework.core.filter.server.ServerAfterFilterChain;
import org.example.irpc.framework.core.filter.server.ServerBeforeFilterChain;
import org.example.irpc.framework.core.filter.server.ServerFilterChain;
import org.example.irpc.framework.core.registy.URL;
import org.example.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.example.irpc.framework.core.serialize.SerializeFactory;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.EXTENSION_LOADER;
import static org.example.irpc.framework.core.common.cache.CommonServerCache.*;
import static org.example.irpc.framework.core.common.constants.RpcConstants.*;
import static org.example.irpc.framework.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

public class Server {

    private static EventLoopGroup bossGroup = null;

    private static EventLoopGroup workerGroup = null;

    private ServerConfig serverConfig;

    private static IRpcListenerLoader iRpcListenerLoader;

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void startApplication() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.SO_KEEPALIVE, true);

        //服务端采用单一长连接的模式，这里所支持的最大连接数应该和机器本身的性能有关
        //连接防护的handler应该绑定在Main-Reactor上
        bootstrap.handler(new MaxConnectionLimitHandler(serverConfig.getMaxConnections()));
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                System.out.println("初始化provider过程");
                ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(serverConfig.getMaxServerRequestData(), delimiter));
                socketChannel.pipeline().addLast(new RpcEncoder());
                socketChannel.pipeline().addLast(new RpcDecoder());
                socketChannel.pipeline().addLast(new ServerHandler());
            }
        });
        this.batchExportUrl();
        // 开始准备接收请求的任务
        SERVER_CHANNEL_DISPATCHER.startDataConsume();
        bootstrap.bind(serverConfig.getServerPort()).sync();
        IS_STARTED = true;
    }

    public void initServerConfig() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
        SERVER_CONFIG = serverConfig;
        // 初始化线程池和队列的配置
        SERVER_CHANNEL_DISPATCHER.init(serverConfig.getServerQueueSize(), serverConfig.getServerBizThreadNums());
        //序列化技术初始化
        String serverSerialize = serverConfig.getServerSerialize();
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        LinkedHashMap<String, Class> serializeFactoryClassMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeFactoryClass = serializeFactoryClassMap.get(serverSerialize);
        if (serializeFactoryClass == null) {
            throw new RuntimeException("no match serialize type for " + serverSerialize);
        }

        SERVER_SERIALIZE_FACTORY = (SerializeFactory) serializeFactoryClass.newInstance();
        //过滤链技术初始化
        EXTENSION_LOADER.loadExtension(IServerFilter.class);
        LinkedHashMap<String, Class> iServerFilterClassMap = EXTENSION_LOADER_CLASS_CACHE.get(IServerFilter.class.getName());
        ServerBeforeFilterChain serverBeforeFilterChain = new ServerBeforeFilterChain();
        ServerAfterFilterChain serverAfterFilterChain = new ServerAfterFilterChain();
        ServerFilterChain serverFilterChain = new ServerFilterChain();
        for (String iServerFilterKey : iServerFilterClassMap.keySet()) {
            Class iServerFilterClass = iServerFilterClassMap.get(iServerFilterKey);
            if(iServerFilterClass==null){
                throw new RuntimeException("no match iServerFilter type for " + iServerFilterKey);
            }
            serverFilterChain.addServerFilter((IServerFilter) iServerFilterClass.newInstance());
        }
        SERVER_AFTER_FILTER_CHAIN = serverAfterFilterChain;
        SERVER_BEFORE_FILTER_CHAIN = serverBeforeFilterChain;
    }

    /**
     * 暴露服务信息
     *
     * @param serviceWrapper
     */
    public void exportService(ServiceWrapper serviceWrapper) {
        Object serviceBean = serviceWrapper.getServiceObj();
        if (serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        if (REGISTRY_SERVICE == null) {
            REGISTRY_SERVICE = new ZookeeperRegister(serverConfig.getRegisterAddr());
        }
        Class interfaceClass = classes[0];
        //需要注册的对象统一放在一个MAP集合中进行管理
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(serverConfig.getApplicationName());
        url.addParameter("host", CommonUtils.getIpAddress());
        url.addParameter("port", String.valueOf(serverConfig.getServerPort()));
        url.addParameter("group", String.valueOf(serviceWrapper.getGroup()));
        url.addParameter("limit", String.valueOf(serviceWrapper.getLimit()));
        PROVIDER_URL_SET.add(url);
        if (CommonUtils.isNotEmpty(serviceWrapper.getServiceToken())) {
            PROVIDER_SERVICE_WRAPPER_MAP.put(interfaceClass.getName(), serviceWrapper);
        }
    }

    /**
     * 这个函数的内部设计就是为了将服务端的具体服务都暴露到注册中心，方便客户端进行调用。
     */
    public void batchExportUrl(){
        Thread task = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (URL url : PROVIDER_URL_SET) {
                REGISTRY_SERVICE.register(url);
            }
        });
        task.start();
    }

    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        Server server = new Server();
        server.initServerConfig();
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        ServiceWrapper dataServiceServiceWrapper = new ServiceWrapper();
        dataServiceServiceWrapper.setServiceObj(new DataServiceImpl());
        dataServiceServiceWrapper.setServiceToken("token-a");
        dataServiceServiceWrapper.setLimit(2);
        dataServiceServiceWrapper.setGroup("dev");
        server.exportService(dataServiceServiceWrapper);
        ApplicationShutdownHook.registryShutdownHook();
        server.startApplication();
    }
}
