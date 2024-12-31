package com.dong.dongrpc.spring.boot.start.bootstrap;

import com.dong.dongrpc.proxy.ServiceProxyFactory;
import com.dong.dongrpc.spring.boot.start.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * 服务消费者启动类
 *
 * 服务消费者需要为接口添加代理
 *
 * 查找有@RpcReference注解的接口，为其添加代理
 */
@Slf4j
public class RpcConsumerBootstrap implements BeanPostProcessor {

    /**
     * 在bean加载后进行处理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 遍历所有属性
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            // 找到是否含有注解@RpcReference
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            // 如果有就添加代理
            if (rpcReference != null){
                Class<?> interfaceClass = rpcReference.interfaceClass();
                // 默认值处理
                if  (interfaceClass == void.class){
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);
                // 添加代理
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    field.set(bean, proxyObject);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理对象失败", e);
                }
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
