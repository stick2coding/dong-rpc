package com.dong.dongrpc.registry;

import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 注册中心接口
 * 这个是一个注册中心提供的基本的方法
 * 然后实际的注册中心可以实现这个接口
 *
 */
public interface DongRegistry {

    /**
     * 初始化注册中心
     * @param registryConfig
     */
    void init(RegistryConfig registryConfig);


    /**
     * 服务端注册服务
     * @param serviceMetaInfo
     */
    void register(ServiceMetaInfo serviceMetaInfo);


    /**
     * 注销服务
     * @param serviceMetaInfo
     */
    void unRegister(ServiceMetaInfo serviceMetaInfo);


    /**
     * 服务发现，通过服务名获取注册中心的服务列表
     * @param serviceName
     * @return
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceName);


    /**
     * 服务销毁
     */
    void destroy();

}
