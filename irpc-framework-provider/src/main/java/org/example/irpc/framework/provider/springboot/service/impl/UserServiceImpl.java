package org.example.irpc.framework.provider.springboot.service.impl;


import org.example.irpc.framework.interfaces.UserService;
import org.example.irpc.framework.spring.starter.common.IRpcService;

/**
 * @Author linhao
 * @Date created in 10:46 下午 2022/3/7
 */
@IRpcService
public class UserServiceImpl implements UserService {

    public void test() {
        System.out.println("test");
    }
}
