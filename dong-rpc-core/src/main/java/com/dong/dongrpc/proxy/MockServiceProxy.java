package com.dong.dongrpc.proxy;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * mock服务，使用jdk动态代理实现
 */
@Slf4j
public class MockServiceProxy implements InvocationHandler {

    /**
     *
     * 调用代理
     * mock就是因为不想调用实际的服务或者实际的服务无法使用的情况
     * 所以就直接返回一个mock的结果
     * 这时候就要模拟原来接口的返回结果
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 拿到原本方法的返回值类型
        Class<?> methodReturnType = method.getReturnType();
        log.info("mock invoke {}", method.getName());

        // 模拟返回
        return getDefaultObject(methodReturnType);
    }

    private Object getDefaultObject(Class<?> type) {

        // 基本数据类型
        if (type.isPrimitive()){
            if (type == int.class){
                return 0;
            }else if (type == long.class){
                return 0L;
            }else if (type == float.class){
                return 0.0f;
            }else if (type == double.class){
                return 0.0d;
            }else if (type == boolean.class){
                return false;
            }
        }
        // 对象
        return null;

    }
}
