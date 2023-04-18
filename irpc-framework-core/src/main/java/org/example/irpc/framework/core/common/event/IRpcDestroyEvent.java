package org.example.irpc.framework.core.common.event;

/**
 * @Author liutingfeng
 * @Date 2023/4/18 3:06 PM
 */
public class IRpcDestroyEvent implements IRpcEvent{
    private Object data;

    public IRpcDestroyEvent(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public IRpcEvent setData(Object data) {
        this.data = data;
        return this;
    }
}
