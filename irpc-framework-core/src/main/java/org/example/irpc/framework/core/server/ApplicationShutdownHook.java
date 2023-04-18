package org.example.irpc.framework.core.server;

import org.example.irpc.framework.core.common.event.IRpcDestroyEvent;
import org.example.irpc.framework.core.common.event.IRpcListenerLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监听java进程被关闭
 * @Author liutingfeng
 * @Date 2023/4/18 3:03 PM
 */
public class ApplicationShutdownHook {

    public static Logger LOGGER = LoggerFactory.getLogger(ApplicationShutdownHook.class);

    /**
     * 注册一个shutdownHook的钩子，当jvm进程关闭的时候触发
     */
    public static void registryShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("[registryShutdownHook] ==== ");
            IRpcListenerLoader.sendSyncEvent(new IRpcDestroyEvent("destroy"));
            System.out.println("destory");
        }));
    }
}
