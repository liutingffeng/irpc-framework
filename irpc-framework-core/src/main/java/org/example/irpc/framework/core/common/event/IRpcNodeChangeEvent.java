package org.example.irpc.framework.core.common.event;

/**
 * @Author liutingfeng
 * @Date 2023/4/17 7:50 PM
 */
public class IRpcNodeChangeEvent implements IRpcEvent{
    private Object data;
    public IRpcNodeChangeEvent(Object data) {
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
