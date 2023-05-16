package org.example.irpc.framework.spring.starter.common;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author liutingfeng
 * @Date 2023/5/16 2:26 PM
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface IRpcService {

    int limit() default 0;

    String group() default "default";

    String serviceToken() default "";
}
