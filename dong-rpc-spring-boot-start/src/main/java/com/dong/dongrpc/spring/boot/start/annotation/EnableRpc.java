package com.dong.dongrpc.spring.boot.start.annotation;

import com.dong.dongrpc.spring.boot.start.bootstrap.RpcConsumerBootstrap;
import com.dong.dongrpc.spring.boot.start.bootstrap.RpcInitBootstrap;
import com.dong.dongrpc.spring.boot.start.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 全局标识项目需要引入rpc框架，执行初始化方法
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RpcInitBootstrap.class, RpcConsumerBootstrap.class, RpcProviderBootstrap.class})
public @interface EnableRpc {

    /**
     * 是否需要启动服务器，改用配置
     */
    //boolean needServer() default true;

}
