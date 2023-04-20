package org.example.irpc.framework.core.server;

/**
 * 服务包装类
 * @Author liutingfeng
 * @Date 2023/4/19 5:00 PM
 */
public class ServiceWrapper {
    /**
     * 对外暴露的具体服务对象
     */
    private Object serviceObj;

    /**
     * 具体暴露服务的分组
     */
    private String group = "default";

    /**
     * 整个应用的token校验
     */
    private String serviceToken = "";

    /**
     * 限流策略
     */
    private Integer limit = -1;

    public Object getServiceObj() {
        return serviceObj;
    }

    public void setServiceObj(Object serviceObj) {
        this.serviceObj = serviceObj;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getServiceToken() {
        return serviceToken;
    }

    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
