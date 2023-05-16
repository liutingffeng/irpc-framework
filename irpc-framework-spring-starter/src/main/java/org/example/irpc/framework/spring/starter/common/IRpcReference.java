package org.example.irpc.framework.spring.starter.common;

import java.lang.annotation.*;

/**
 * @Author liutingfeng
 * @Date 2023/5/16 2:26 PM
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IRpcReference {
    String url() default "";

    String group() default "default";

    String serviceToken() default "";

    int timeOut() default 3000;

    int retry() default 1;

    boolean async() default false;
}
