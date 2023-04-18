package org.example.irpc.framework.core.common.event;

import org.example.irpc.framework.core.common.event.listener.ProviderNodeDataChangeListener;
import org.example.irpc.framework.core.common.event.listener.ServiceDestroyListener;
import org.example.irpc.framework.core.common.event.listener.ServiceUpdateListener;
import org.example.irpc.framework.core.common.utils.CommonUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IRpcListenerLoader {

    private static List<IRpcListener> iRpcListenerList = new ArrayList<>();

    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(3);

    public static void registerListener(IRpcListener iRpcListener) {
        iRpcListenerList.add(iRpcListener);
    }

    public void init() {
        registerListener(new ServiceUpdateListener());
        registerListener(new ServiceDestroyListener());
        registerListener(new ProviderNodeDataChangeListener());
    }

    /**
     * 获取接口上的泛型T
     *
     * @param o     接口
     */
    public static Class<?> getInterfaceT(Object o) {
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    /**
     * 同步事件处理，可能会堵塞
     *
     * @param iRpcEvent
     */
    public static void sendSyncEvent(IRpcEvent iRpcEvent) {
        System.out.println(iRpcListenerList);
        if (CommonUtils.isEmptyList(iRpcListenerList)) {
            return;
        }
        for (IRpcListener listener : iRpcListenerList) {
            Class<?> type = getInterfaceT(listener);
            if (type.equals(iRpcEvent.getClass())) {
                try {
                    listener.callBack(iRpcEvent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void sendEvent(IRpcEvent iRpcEvent) {
        if (CommonUtils.isEmptyList(iRpcListenerList)) {
            return;
        }
        for (IRpcListener listener : iRpcListenerList) {
            Class<?> type = getInterfaceT(listener);
            if (type.equals(iRpcEvent.getClass())) {
                eventThreadPool.execute(() -> {
                    try {
                        listener.callBack(iRpcEvent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }
}
