package org.example.irpc.framework.core.common.cache;

import org.example.irpc.framework.core.common.config.ServerConfig;
import org.example.irpc.framework.core.filter.server.ServerFilterChain;
import org.example.irpc.framework.core.registy.RegistryService;
import org.example.irpc.framework.core.registy.URL;
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
    public static RegistryService REGISTRY_SERVICE;
    public static SerializeFactory SERVER_SERIALIZE_FACTORY;
    public static ServerConfig SERVER_CONFIG;
    public static ServerFilterChain SERVER_FILTER_CHAIN;
    public static final Map<String, ServiceWrapper> PROVIDER_SERVICE_WRAPPER_MAP = new ConcurrentHashMap<>();
}
