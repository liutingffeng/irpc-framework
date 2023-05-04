package org.example.irpc.framework.core.spi.jdk;

/**
 * @Author liutingfeng
 * @Date 2023/4/21 11:15 AM
 */
public class DefaultISpiTest implements ISpiTest{
    @Override
    public void doTest() {
        System.out.println("执行测试方法");
    }
}
