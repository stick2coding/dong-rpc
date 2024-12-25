package com.dong.example.provider;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.registry.RegistryFactory;
import com.dong.dongrpc.server.DongHttpServer;
import com.dong.dongrpc.server.VertxDongHttpServer;
import com.dong.example.common.service.UserService;
import com.dong.example.provider.service.impl.UserServiceImpl;

public class ExampleProvider {

    public static void main( String[] args ) {
        // 初始化RPC框架
        RpcApplication.init();

        // 提供服务
        System.out.println( "Hello! provider start success!" );

        // 将自身的服务注册到注册中心
        String serviceName = UserService.class.getName();
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 服务提供者rpc配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 注册中心配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        DongRegistry registry = RegistryFactory.getInstance(registryConfig.getRegistryType());
        // 服务元信息
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e){
            throw new RuntimeException("注册服务失败...");
        }

        // 服务提供者引入RPC框架，然后启动rpc框架中的web服务器
        DongHttpServer dongHttpServer = new VertxDongHttpServer();
        System.out.println("当前web服务器端口：" + RpcApplication.getRpcConfig().getServerPort());
        dongHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

}
