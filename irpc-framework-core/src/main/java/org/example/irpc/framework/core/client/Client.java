package org.example.irpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import org.example.irpc.framework.core.common.RpcDecoder;
import org.example.irpc.framework.core.common.RpcEncoder;
import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.common.RpcProtocol;
import org.example.irpc.framework.core.common.config.ClientConfig;
import org.example.irpc.framework.core.common.config.PropertiesBootstrap;
import org.example.irpc.framework.core.common.event.IRpcListenerLoader;
import org.example.irpc.framework.core.common.router.IRouter;
import org.example.irpc.framework.core.common.router.RandomRouterImpl;
import org.example.irpc.framework.core.common.router.RotateRouterImpl;
import org.example.irpc.framework.core.common.utils.CommonUtils;
import org.example.irpc.framework.core.filter.IClientFilter;
import org.example.irpc.framework.core.filter.client.ClientFilterChain;
import org.example.irpc.framework.core.filter.client.ClientLogFilterImpl;
import org.example.irpc.framework.core.filter.client.DirectInvokeFilterImpl;
import org.example.irpc.framework.core.filter.client.GroupFilterImpl;
import org.example.irpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.example.irpc.framework.core.registy.RegistryService;
import org.example.irpc.framework.core.registy.URL;
import org.example.irpc.framework.core.registy.zookeeper.AbstractRegister;
import org.example.irpc.framework.core.registy.zookeeper.CuratorZookeeperClient;
import org.example.irpc.framework.core.registy.zookeeper.ZookeeperRegister;
import org.example.irpc.framework.core.serialize.SerializeFactory;
import org.example.irpc.framework.core.serialize.fastjson.FastJsonSerializeFactory;
import org.example.irpc.framework.core.serialize.hessian.HessianSerializeFactory;
import org.example.irpc.framework.core.serialize.jdk.JdkSerializeFactory;
import org.example.irpc.framework.core.serialize.kryo.KryoSerializeFactory;
import org.example.irpc.framework.interfaces.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.example.irpc.framework.core.common.cache.CommonClientCache.*;
import static org.example.irpc.framework.core.common.constants.RpcConstants.*;
import static org.example.irpc.framework.core.spi.ExtensionLoader.EXTENSION_LOADER_CLASS_CACHE;

public class Client {
    private Logger logger = LoggerFactory.getLogger(Client.class);

    private ClientConfig clientConfig;

    public static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private Bootstrap bootstrap = new Bootstrap();

    private IRpcListenerLoader iRpcListenerLoader;

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    private RpcReference initClientApplication() {
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        bootstrap.group(clientGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ByteBuf delimiter = Unpooled.copiedBuffer(DEFAULT_DECODE_CHAR.getBytes());
                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(clientConfig.getMaxServerRespDataSize(), delimiter));
                        //管道中初始化一些逻辑，这里包含了上边所说的编解码器和客户端响应类
                        ch.pipeline().addLast(new RpcEncoder());
                        ch.pipeline().addLast(new RpcDecoder());
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        CLIENT_CONFIG = this.clientConfig;
        RpcReference rpcReference = null;
        if ("javassist".equals(clientConfig.getProxyType())) {
//            rpcReference = new RpcReference(new JavassistProxyFactory());
        } else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }
//        //常规的链接netty服务端
//        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();
//        logger.info("============ 服务启动 ============");
//        this.startClient(channelFuture);
//        //这里注入了一个代理工厂，这个代理类在下文会仔细介绍
//        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }

    /**
     * 启动服务之前需要预先订阅对应的dubbo服务
     *
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean) {
        if (ABSTRACT_REGISTER == null) {
            try {
                // 使用自定义的SPI机制去加载配置
                EXTENSION_LOADER.loadExtension(RegistryService.class);
                Map<String, Class> registerMap = EXTENSION_LOADER_CLASS_CACHE.get(RegistryService.class.getName());
                Class registerClass = registerMap.get(clientConfig.getRegisterType());
                // 真正实例化对象的位置
                ABSTRACT_REGISTER = (AbstractRegister) registerClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("registryServiceType unKnow,error is ", e);
            }
            ABSTRACT_REGISTER = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }
        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtils.getIpAddress());
        Map<String, String> result = ABSTRACT_REGISTER.getServiceWeightMap(serviceBean.getName());
        URL_MAP.put(serviceBean.getName(), result);
        ABSTRACT_REGISTER.subscribe(url);
    }

    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        for (URL providerURL : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = ABSTRACT_REGISTER.getProviderIps(providerURL.getServiceName());
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerURL.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    logger.error("[doConnectServer] connect fail ", e);
                }
            }
            URL url = new URL();
            url.addParameter("servicePath",providerURL.getServiceName()+"/provider");
            url.addParameter("providerIps", JSON.toJSONString(providerIps));
            // 客户端在此新增一个订阅的功能
            ABSTRACT_REGISTER.doAfterSubscribe(url);
        }
    }

    /**
     * 开启发送线程，专门从事将数据包发送给服务端，起到一个解耦的效果
     */
    private void startClient() {
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }

    class AsyncSendJob implements Runnable {

        public AsyncSendJob() {
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RpcInvocation data = SEND_QUEUE.take();
                    //将RpcInvocation封装到RpcProtocol对象中，然后发送给服务端，这里正好对应了上文中的ServerHandler
//                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(CLIENT_SERIALIZE_FACTORY.serialize(data));
//                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data);
                    //netty的通道负责发送数据给服务端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * todo
     * 后续可以考虑加入spi
     */
    private void initClientConfig() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // 初始化路由策略
        EXTENSION_LOADER.loadExtension(IRouter.class);
        String routerStrategy = clientConfig.getRouterStrategy();
        LinkedHashMap<String, Class> iRouterMap = EXTENSION_LOADER_CLASS_CACHE.get(IRouter.class.getName());
        Class iRouterClass = iRouterMap.get(routerStrategy);
        if (iRouterClass == null) {
            throw new RuntimeException("no match routerStrategy for " + routerStrategy);
        }
        IROUTER = (IRouter) iRouterClass.newInstance();

        //初始化序列化框架
        EXTENSION_LOADER.loadExtension(SerializeFactory.class);
        String clientSerialize = clientConfig.getClientSerialize();
        LinkedHashMap<String, Class> serializeMap = EXTENSION_LOADER_CLASS_CACHE.get(SerializeFactory.class.getName());
        Class serializeFactoryClass = serializeMap.get(clientSerialize);
        if (serializeFactoryClass == null) {
            throw new RuntimeException("no match serialize type for " + clientSerialize);
        }
        CLIENT_SERIALIZE_FACTORY = (SerializeFactory) serializeFactoryClass.newInstance();

        //初始化过滤链
        EXTENSION_LOADER.loadExtension(IClientFilter.class);
        ClientFilterChain clientFilterChain = new ClientFilterChain();
        LinkedHashMap<String, Class> iClientMap = EXTENSION_LOADER_CLASS_CACHE.get(IClientFilter.class.getName());
        for (String implClassName : iClientMap.keySet()) {
            Class iClientFilterClass = iClientMap.get(implClassName);
            if (iClientFilterClass == null) {
                throw new RuntimeException("no match iClientFilter for " + iClientFilterClass);
            }
            clientFilterChain.addClientFilter((IClientFilter) iClientFilterClass.newInstance());
        }
        CLIENT_FILTER_CHAIN = clientFilterChain;
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
//        ClientConfig clientConfig = new ClientConfig();
//        client.setClientConfig(clientConfig);
        RpcReference rpcReference = client.initClientApplication();
        client.initClientConfig();
        RpcReferenceWrapper<DataService> rpcReferenceWrapper = new RpcReferenceWrapper<>();
        rpcReferenceWrapper.setAimClass(DataService.class);
        rpcReferenceWrapper.setGroup("dev");
        rpcReferenceWrapper.setServiceToken("token-a");
        rpcReferenceWrapper.setAsync(true);
        rpcReferenceWrapper.setTimeOut(1000);
        //超时重试次数
        rpcReferenceWrapper.setRetry(1);
        rpcReferenceWrapper.setAsync(false);
        // 在初始化之前必须要设置对应的上下文
        DataService dataService = rpcReference.get(rpcReferenceWrapper);
        client.doSubscribeService(DataService.class);
        ConnectionHandler.setBootstrap(client.getBootstrap());
        client.doConnectServer();
        client.startClient();
        for (int i = 0; i < 1; i++) {
            try {
                String result = dataService.sendData("test");
                System.out.println(result);
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
