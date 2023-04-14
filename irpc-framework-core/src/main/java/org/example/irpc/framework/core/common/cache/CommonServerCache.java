package org.example.irpc.framework.core.common.cache;

import org.example.irpc.framework.core.registy.URL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CommonServerCache {
    public static final Map<String, Object> PROVIDER_CLASS_MAP = new HashMap<>();
    public static final Set<URL> PROVIDER_URL_SET = new HashSet<>();
}
