package com.dong.dongrpc.loadbalancer;

import cn.hutool.core.collection.CollUtil;
import com.dong.dongrpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 随机
 *
 * 使用随机数即可
 */
public class RandomLoadBalancer implements LoadBalancer {

    /**
     * 随机
     */
    private final Random random = new Random();

    /**
     * 随机
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

        return serviceMetaInfoList.get(random.nextInt(size));
    }
}
