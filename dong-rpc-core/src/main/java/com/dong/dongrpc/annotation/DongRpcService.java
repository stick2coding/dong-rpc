package com.dong.dongrpc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 这里是想通过注解的方式来驱动服务注册，不过实现不太合理
 * 具体的注解驱动要查看springboot-start内的代码
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DongRpcService {

    String name() default "";

    String implPath() default "";

}
