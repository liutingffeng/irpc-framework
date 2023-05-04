package org.example.irpc.framework.core.spi.jdk;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @Author liutingfeng
 * @Date 2023/4/21 11:15 AM
 */
public class TestSpiDemo {

    public static void doTest(ISpiTest iSpiTest) {
        System.out.println("begin");
        iSpiTest.doTest();
        System.out.println("end");
    }

    public static void main(String[] args) {
        ServiceLoader<ISpiTest> serviceLoader = ServiceLoader.load(ISpiTest.class);
        Iterator<ISpiTest> iSpiTestIterator = serviceLoader.iterator();
        while (iSpiTestIterator.hasNext()) {
            ISpiTest iSpiTest = iSpiTestIterator.next();
            doTest(iSpiTest);
        }
    }
}
