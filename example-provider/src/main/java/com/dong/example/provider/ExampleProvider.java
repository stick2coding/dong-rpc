package com.dong.example.provider;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.annotation.DongRpcService;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.registry.RegistryFactory;
import com.dong.dongrpc.server.DongHttpServer;
import com.dong.dongrpc.server.VertxDongHttpServer;
import com.dong.dongrpc.utils.ConfigUtils;
import com.dong.example.common.service.UserService;
import com.dong.example.provider.config.ProviderConfig;
import com.dong.example.provider.service.impl.UserServiceImpl;
import org.reflections.Reflections;

import java.util.ServiceLoader;
import java.util.Set;

public class ExampleProvider {

    private static ProviderConfig providerConfig;

    public static void main( String[] args ) {
        // 提供服务
        System.out.println( "Hello! provider start success!" );
        // 初始化RPC框架
        RpcApplication.init();

        // 提供者配置加载
        LoadConsumerConfig();

        //服务注册
        serviceRegistry();

        // 服务提供者引入RPC框架，然后启动rpc框架中的web服务器
        DongHttpServer dongHttpServer = new VertxDongHttpServer();
        dongHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

    /**
     * 加载自身配置
     */
    private static void LoadConsumerConfig() {
        providerConfig = ConfigUtils.loadConfig(ProviderConfig.class, "provider");
        System.out.println(providerConfig);
    }

    /**
     * 注册所有服务
     */
    private static void serviceRegistry() {
        // 服务提供者rpc配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 注册中心配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        DongRegistry registry = RegistryFactory.getInstance(registryConfig.getRegistryType());

        //获取当前项目的路径
        Reflections reflections = new Reflections(providerConfig.getDongRpcServicePath());
        Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(DongRpcService.class);
        for (Class<?> serviceClass : serviceClasses) {
            // 将自身的服务注册到注册中心（这里改为注解文件扫描的方式）
            //String interfaceName = UserService.class.getName();
            // 先将接口以及实现类放到本地缓存，以便于后续框架可以通过请求中的接口名在本地缓存中找到实现类
            String interfaceName = serviceClass.getName();
            DongRpcService dongRpcService = serviceClass.getAnnotation(DongRpcService.class);
            String serviceName = dongRpcService.name();
            String serviceImplPath = dongRpcService.implPath();
            Class<?> serviceImplClass = null;
            try {
                serviceImplClass = Class.forName(serviceImplPath);
            } catch (ClassNotFoundException e) {
                System.out.println("服务实现类未找到...");
                continue;
            }
            LocalRegistry.register(interfaceName, serviceImplClass);
            // 同时将服务到注册中心
            // 服务元信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
                System.out.println("注册服务成功..." + serviceName);
            } catch (Exception e){
                throw new RuntimeException("注册服务失败...");
            }
        }
    }

}
