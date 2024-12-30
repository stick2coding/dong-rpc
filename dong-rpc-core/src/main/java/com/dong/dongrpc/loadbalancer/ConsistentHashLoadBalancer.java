package com.dong.dongrpc.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import com.dong.dongrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性hash负载均衡
 */
public class ConsistentHashLoadBalancer implements LoadBalancer{

    // 先构建一个hash环来存储虚拟节点
    private final TreeMap<Integer, ServiceMetaInfo> hashCircle = new TreeMap<>();

    // 虚拟节点的数量
    private static final int VIRTUAL_NODE_SIZE = 100;

    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParam, List<ServiceMetaInfo> serviceMetaInfoList) {
        //先校验
        if (CollUtil.isEmpty(serviceMetaInfoList)){
            return null;
        }

        // 先构建虚拟节点
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_SIZE; i++) {
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                hashCircle.put(hash, serviceMetaInfo);
            }
        }

        // 获取请求的hash
        int requesthash = getHash(requestParam);

        // 寻找最近的节点，就是找第一个hash比请求大的节点
        Map.Entry<Integer, ServiceMetaInfo> entry = hashCircle.ceilingEntry(requesthash);
        // 如果找不到，就取第一个
        if (entry == null){
            return hashCircle.firstEntry().getValue();
        }
        return entry.getValue();
    }

    /**
     * hash算法
     * @param obj
     * @return
     */
    private Integer getHash(Object obj) {
        return obj.hashCode();
    }
}
