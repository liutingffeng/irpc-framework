package org.example.irpc.framework.core.dispatcher;

import org.example.irpc.framework.core.common.RpcInvocation;
import org.example.irpc.framework.core.common.RpcProtocol;
import org.example.irpc.framework.core.common.exception.IRpcException;
import org.example.irpc.framework.core.server.ServerChannelReadData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.*;

import static org.example.irpc.framework.core.common.cache.CommonServerCache.*;

/**
 * @Author liutingfeng
 * @Date 2023/5/10 7:55 PM
 */
public class ServerChannelDispatcher {

    private BlockingQueue<ServerChannelReadData> RPC_DATA_QUEUE;

    private ExecutorService executorService;

    public ServerChannelDispatcher() {

    }

    public void init(int queueSize, int bizThreadNums) {
        RPC_DATA_QUEUE = new ArrayBlockingQueue(queueSize);
        executorService = new ThreadPoolExecutor(bizThreadNums, bizThreadNums, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(512));
    }

    public void add(ServerChannelReadData serverChannelReadData) {
        RPC_DATA_QUEUE.add(serverChannelReadData);
    }

    class ServerJobCoreHandle implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    ServerChannelReadData serverChannelReadData = RPC_DATA_QUEUE.take();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RpcProtocol rpcProtocol = serverChannelReadData.getRpcProtocol();
                                RpcInvocation rpcInvocation = SERVER_SERIALIZE_FACTORY.deserialize(rpcProtocol.getContent(), RpcInvocation.class);
                                //执行过滤链路
                                //前置过滤器
                                try {
                                    SERVER_BEFORE_FILTER_CHAIN.doFilter(rpcInvocation);
                                } catch (Exception cause) {
                                    if (cause instanceof IRpcException) {
                                        IRpcException rpcException = (IRpcException) cause;
                                        RpcInvocation reqParam = rpcException.getRpcInvocation();
                                        rpcInvocation.setE(rpcException);
                                        byte[] body = SERVER_SERIALIZE_FACTORY.serialize(reqParam);
                                        RpcProtocol respRpcProtocol = new RpcProtocol(body);
                                        serverChannelReadData.getChannelHandlerContext().writeAndFlush(respRpcProtocol);
                                        return;
                                    }
                                }
                                //这里的PROVIDER_CLASS_MAP就是一开始预先在启动时候存储的Bean集合
                                Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
                                Method[] methods = aimObject.getClass().getDeclaredMethods();
                                Object result = null;
                                for (Method method : methods) {
                                    if (method.getName().equals(rpcInvocation.getTargetMethod())) {
                                        // 通过反射找到目标对象，然后执行目标方法并返回对应值
                                        if (method.getReturnType().equals(Void.class)) {
                                            try {
                                                method.invoke(aimObject, rpcInvocation.getArgs());
                                            } catch (Exception e) {
                                                //业务异常
                                                rpcInvocation.setE(e);
                                            }
                                        } else {
                                            try {
                                                result = method.invoke(aimObject, rpcInvocation.getArgs());
                                            } catch (Exception e) {
                                                //业务异常
                                                rpcInvocation.setE(e);
                                            }
                                        }
                                        break;
                                    }
                                }
                                rpcInvocation.setResponse(result);
                                //后置过滤器
                                SERVER_AFTER_FILTER_CHAIN.doFilter(rpcInvocation);
                                RpcProtocol resRpcProtocol = new RpcProtocol(SERVER_SERIALIZE_FACTORY.serialize(rpcInvocation));
                                serverChannelReadData.getChannelHandlerContext().writeAndFlush(resRpcProtocol);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void startDataConsume() {
        Thread thread = new Thread(new ServerJobCoreHandle());
        thread.start();
    }
}
