package com.dong.dongrpc.bootstrap;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.annotation.DongRpcService;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.model.ServiceRegisterInfo;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.registry.RegistryFactory;
import com.dong.dongrpc.server.tcp.TcpServer;
import com.dong.dongrpc.server.tcp.VertxTcpServer;
import com.dong.dongrpc.utils.ConfigUtils;

import java.util.List;
import java.util.Set;

public class ProviderBootstrap {


    public static void init(List<ServiceRegisterInfo> serviceRegisterInfoList){
        // 提供服务
        System.out.println( "Hello! provider starting!" );
        // 初始化RPC框架
        RpcApplication.init();
        // 服务提供者rpc配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 注册中心配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        DongRegistry registry = RegistryFactory.getInstance(registryConfig.getRegistryType());

        //获取当前项目的路径
        for (ServiceRegisterInfo serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());
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

        //启动服务器
        TcpServer tcpServer = new VertxTcpServer();
        tcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

}
