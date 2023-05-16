package org.example.irpc.framework.core.common.cache;

import org.example.irpc.framework.core.common.ServerServiceSemaphoreWrapper;
import org.example.irpc.framework.core.common.config.ServerConfig;
import org.example.irpc.framework.core.dispatcher.ServerChannelDispatcher;
import org.example.irpc.framework.core.filter.server.ServerAfterFilterChain;
import org.example.irpc.framework.core.filter.server.ServerBeforeFilterChain;
import org.example.irpc.framework.core.filter.server.ServerFilterChain;
import org.example.irpc.framework.core.registy.RegistryService;
import org.example.irpc.framework.core.registy.URL;
import org.example.irpc.framework.core.registy.zookeeper.AbstractRegister;
import org.example.irpc.framework.core.serialize.SerializeFactory;
import org.example.irpc.framework.core.server.ServiceWrapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommonServerCache {
    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<>();
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();
    public static AbstractRegister REGISTRY_SERVICE;
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;
    public static ServerConfig SERVER_CONFIG;
    public static ServerFilterChain SERVER_FILTER_CHAIN;
    public static ServerBeforeFilterChain SERVER_BEFORE_FILTER_CHAIN;
    public static ServerAfterFilterChain SERVER_AFTER_FILTER_CHAIN;
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();
    //分发器对象
    public static ServerChannelDispatcher SERVER_CHANNEL_DISPATCHER = new ServerChannelDispatcher();
    public static Boolean IS_STARTED = false;
    public static final Map<String, ServerServiceSemaphoreWrapper> SERVER_SERVICE_SEMAPHORE_MAP = new ConcurrentHashMap<>(64);
}
