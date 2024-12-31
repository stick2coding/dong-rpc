package com.dong.dongrpc.utils;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.constant.RpcConstant;
import com.dong.dongrpc.loadbalancer.LoadBalancer;
import com.dong.dongrpc.loadbalancer.LoadBalancerFactory;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.RegistryFactory;

import java.util.List;
import java.util.Map;

public class RequestUtils {

    /**
     * 服务发现与选择
     * @param obj
     * @param seachServiceMetaInfo
     * @return
     */
    public static ServiceMetaInfo serviceDiscoverAndSelect(Map<String, Object> obj, ServiceMetaInfo seachServiceMetaInfo){
        List<ServiceMetaInfo> serviceMetaInfoList = serviceDiscovery(seachServiceMetaInfo);
        ServiceMetaInfo selectServiceMetaInfo = serviceSelect(obj, serviceMetaInfoList);
        return selectServiceMetaInfo;
    }

    /**
     * 服务发现
     * @param serviceMetaInfo
     * @return
     */
    public static List<ServiceMetaInfo> serviceDiscovery(ServiceMetaInfo serviceMetaInfo) {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 拿到对应的注册中心实例
        DongRegistry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistryType());
        // 搜索节点 key userService:1.0
        return registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
    }

    /**
     * 服务选择
     * @param obj
     * @return
     */
    public static ServiceMetaInfo serviceSelect(Map<String, Object> obj, List<ServiceMetaInfo> serviceMetaInfoList){
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancerType());
        return loadBalancer.select(obj, serviceMetaInfoList);
    }

}
