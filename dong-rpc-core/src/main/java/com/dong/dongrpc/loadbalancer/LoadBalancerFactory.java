package com.dong.dongrpc.loadbalancer;

import com.dong.dongrpc.utils.SpiLoder;

public class LoadBalancerFactory {

    static {
        SpiLoder.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();


    /**
     * 获取负载均衡器
     * @param key
     * @return
     */
    public static LoadBalancer getInstance(String key) {
        return  SpiLoder.getInstance(LoadBalancer.class, key);
    }

}
