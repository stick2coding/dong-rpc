package com.dong.example.provider;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.annotation.DongRpcService;
import com.dong.dongrpc.bootstrap.ProviderBootstrap;
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
import com.dong.example.provider.config.ProviderConfig;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ExampleProviderBootStrap {

    private static ProviderConfig providerConfig;

    public static void main( String[] args ) {
        providerConfig = ConfigUtils.loadConfig(ProviderConfig.class, "provider");
        // 获取要注册的服务列表
        List<ServiceRegisterInfo> serviceRegisterInfoList = loadAllServiceRegisterInfo();

        // 调用框架的初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }

    private static List<ServiceRegisterInfo> loadAllServiceRegisterInfo() {
        List<ServiceRegisterInfo> serviceRegisterInfoList = new ArrayList<>();
        //获取当前项目的路径
        Reflections reflections = new Reflections(providerConfig.getDongRpcServicePath());
        Set<Class<?>> serviceClasses = reflections.getTypesAnnotatedWith(DongRpcService.class);
        for (Class<?> serviceClass : serviceClasses) {
            ServiceRegisterInfo serviceRegisterInfo = new ServiceRegisterInfo();
            // 将自身的服务注册到注册中心（这里改为注解文件扫描的方式）
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
            serviceRegisterInfo.setServiceName(serviceName);
            serviceRegisterInfo.setImplClass(serviceImplClass);
            serviceRegisterInfoList.add(serviceRegisterInfo);
        }
        return serviceRegisterInfoList;
    }

    /**
     * 加载自身配置
     */
    private static void LoadProviderConfig() {
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
