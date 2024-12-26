package com.dong.dongrpc.registry;

import com.dong.dongrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消费端服务缓存（多个服务就需要用MAP来存储）
 */
public class RegistryMultiServiceCache {

    /**
     * 本地缓存的服务列表
     */
    Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 写
     */
    public void writeCache(String serviceKey, List<ServiceMetaInfo> newServiceCache) {
        serviceCache.put(serviceKey, newServiceCache);
    }

    /**
     * 读
     * @return
     */
    public List<ServiceMetaInfo> readCache(String serviceKey) {
        return serviceCache.get(serviceKey);
    }

    /**
     * 清理缓存
     */
    public void clearCache(String serviceKey) {
        serviceCache.remove(serviceKey);
    }

}
