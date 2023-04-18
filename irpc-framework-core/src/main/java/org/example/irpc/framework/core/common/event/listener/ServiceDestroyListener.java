package org.example.irpc.framework.core.common.event.listener;

import org.example.irpc.framework.core.common.event.IRpcDestroyEvent;
import org.example.irpc.framework.core.common.event.IRpcListener;
import org.example.irpc.framework.core.registy.URL;

import static org.example.irpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;
import static org.example.irpc.framework.core.common.cache.CommonServerCache.REGISTRY_SERVICE;

/**
 * @Author liutingfeng
 * @Date 2023/4/18 3:09 PM
 */
public class ServiceDestroyListener implements IRpcListener<IRpcDestroyEvent> {
    @Override
    public void callBack(IRpcDestroyEvent iRpcDestroyEvent) {
        for (URL url : PROVIDER_URL_SET) {
            REGISTRY_SERVICE.unRegister(url);
            System.out.println("ABC:" + url.getServiceName());
        }
//        PROVIDER_URL_SET.stream().forEach(e -> {
//            REGISTRY_SERVICE.unRegister(e);
//            System.out.println(e.getServiceName());
//        });
    }
}
