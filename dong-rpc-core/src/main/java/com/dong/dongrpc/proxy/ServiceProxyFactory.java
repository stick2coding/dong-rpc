package com.dong.dongrpc.proxy;

import com.dong.dongrpc.RpcApplication;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂（用于创建代理对象）
 * 由于增加了mock代理，所以需要通过读取配置文件类判断加载哪种代理
 *
 */
public class ServiceProxyFactory {

    /**
     * 根据服务类会获取代理对象
     * @param serviceClass
     * @return
     * @param <T>
     */
    public static <T> T getProxy(Class<T> serviceClass){
        Boolean mock = RpcApplication.getRpcConfig().isMock();
        if (mock){
            return getMockProxy(serviceClass);
        }

        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                new ServiceProxy()
        );
    }

    /**
     * mock代理
     * @param serviceClass
     * @return
     * @param <T>
     */
    private static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class<?>[]{serviceClass},
                new MockServiceProxy()
        );
    }

}
