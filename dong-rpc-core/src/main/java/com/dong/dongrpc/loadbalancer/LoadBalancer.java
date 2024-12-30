package com.dong.dongrpc.loadbalancer;

import com.dong.dongrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;

/**
 * 负载均衡通用接口（消费者使用）
 */
public interface LoadBalancer {

    /**
     * 选择服务
     * @param requestParam
     * @param serviceMetaInfoList
     * @return
     */
    ServiceMetaInfo select(Map<String, Object> requestParam,  List<ServiceMetaInfo> serviceMetaInfoList);

}
