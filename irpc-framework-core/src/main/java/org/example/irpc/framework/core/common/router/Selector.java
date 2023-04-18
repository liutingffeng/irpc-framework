package org.example.irpc.framework.core.common.router;

/**
 * @Author liutingfeng
 * @Date 2023/4/17 5:29 PM
 */
public class Selector {
    /**
     * 服务命名
     * eg: com.sise.test.DataService
     */
    private String providerServiceName;

    public String getProviderServiceName() {
        return providerServiceName;
    }

    public void setProviderServiceName(String providerServiceName) {
        this.providerServiceName = providerServiceName;
    }
}
