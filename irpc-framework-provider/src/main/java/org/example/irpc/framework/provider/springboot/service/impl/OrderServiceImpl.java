package org.example.irpc.framework.provider.springboot.service.impl;

import org.example.irpc.framework.interfaces.OrderService;
import org.example.irpc.framework.spring.starter.common.IRpcService;

import java.util.Arrays;
import java.util.List;

/**
 * @Author liutingfeng
 * @Date 2023/5/16 3:23 PM
 */
@IRpcService(serviceToken = "order-token", group = "order-group", limit = 2)
public class OrderServiceImpl implements OrderService {
    @Override
    public List<String> getOrderNoList() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Arrays.asList("item1","item2");
    }

    @Override
    public String testMaxData(int i) {
        StringBuffer stb = new StringBuffer();
        for(int j=0;j<i;j++){
            stb.append("1");
        }
        return stb.toString();
    }
}
