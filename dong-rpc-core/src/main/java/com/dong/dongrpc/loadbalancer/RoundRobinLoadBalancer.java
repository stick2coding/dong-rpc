package com.dong.dongrpc.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import com.dong.dongrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 轮询
 *
 * 实现逻辑：就是搞一个自增长的数，然后对列表数量取模
 */
public class RoundRobinLoadBalancer implements LoadBalancer{

    /**
     * 当前游标
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);


    /**
     *
     * @param requestParam
     * @param serviceMetaInfoList
     * @return
     */
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParam, List<ServiceMetaInfo> serviceMetaInfoList) {
        //先校验
        if (CollUtil.isEmpty(serviceMetaInfoList)){
            return null;
        }

        // 如果只有一个服务就不需要轮询了
        int size = serviceMetaInfoList.size();
        if (size == 1){
            return serviceMetaInfoList.get(0);
        }
        // 取模
        int index = currentIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
