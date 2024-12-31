package com.dong.dongrpc.config;

import com.dong.dongrpc.fault.retry.RetryStrategyKeys;
import com.dong.dongrpc.fault.tolerant.TolerantStrategyKeys;
import com.dong.dongrpc.loadbalancer.LoadBalancerKeys;
import com.dong.dongrpc.serializer.SerializerKeys;
import lombok.Data;

@Data
public class RpcConfig {

    /**
     * 服务名称
     */
    private String name = "dong-rpc";

    /**
     * 版本号
     */
    private String version = "1.0.0";

    /**
     * 服务地址
     */
    private String serverHost = "127.0.0.1";

    /**
     * 服务端口
     */
    private int serverPort = 8888;

    /**
     * 是否开启mock
     */
    private boolean mock = false;

    /**
     * 是否需要server
     */
    private boolean needServer = true;

    /**
     * 序列化器类型
     */
    private String serializerType = SerializerKeys.JDK_SERIALIZER;

    /**
     * 负载均衡类型
     */
    private String loadBalancerType = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategyType = RetryStrategyKeys.FIXED_INTERVAL;

    /**
     * 容错策略
     */
    private String tolerantStrategyType = TolerantStrategyKeys.FAIL_FAST;

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();

}
