package com.dong.dongrpc.spring.boot.start.bootstrap;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.model.ServiceRegisterInfo;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.registry.RegistryFactory;
import com.dong.dongrpc.spring.boot.start.annotation.RpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * 服务提供者需要做的就是 将服务注册到注册中心
 *
 * 需要扫描所有@RpcService注解的类
 *
 * 可以通过监听bean的加载来实现
 *
 * 也可以通过主动扫描文件夹来实现
 */
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * 这里我们在每个bean初始化后判断是否是rpc，然后进行处理
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 查看是否有@RpcService注解，这个注解是放在实现类上的
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);
        if (rpcService != null){
            //存在则需要注册
            // 获取服务的基本信息
            Class<?> interfaceClass = rpcService.interfaceClass();
            // 默认值
            if (interfaceClass == void.class){
                interfaceClass = beanClass.getInterfaces()[0];
            }
            String serviceName = interfaceClass.getName();
            System.out.println("注册服务:" + serviceName);
            String serviceVersion = rpcService.serviceVersion();
            //本地注册
            LocalRegistry.register(serviceName, beanClass);

            // 全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            DongRegistry registry = RegistryFactory.getInstance(registryConfig.getRegistryType());
            // 服务元信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e){
                throw new RuntimeException("注册服务失败...");
            }
        }


        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

}
