package com.dong.dongrpc.registry;

import com.dong.dongrpc.model.ServiceMetaInfo;

import java.util.List;

/**
 * 消费端服务缓存（如果只有一个服务，用list存储）（服务消费者使用）
 */
public class RegistrySingleServiceCache {

    /**
     * 本地缓存的服务列表
     */
    List<ServiceMetaInfo> serviceCache;

    /**
     * 写
     */
    public void writeCache(List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache = newServiceCache;
    }

    /**
     * 读
     * @return
     */
    public List<ServiceMetaInfo> readCache() {
        return this.serviceCache;
    }

    /**
     * 清理缓存
     */
    public void clearCache() {
        this.serviceCache = null;
    }

}
