package com.zss.seckill.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Auther: zss
 * @Date: 2022/12/15 15:22
 * @Description: 通用接口限流
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {
    int second();

    int maxCount();

    boolean needLogin() default true;
}
