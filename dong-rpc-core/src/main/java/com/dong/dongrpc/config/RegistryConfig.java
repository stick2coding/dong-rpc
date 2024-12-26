package com.dong.dongrpc.config;

import lombok.Data;


/**
 * 注册中心配置
 */
@Data
public class RegistryConfig {
    /**
     * 注册中心类型
     */
    private String registryType = "etcd";

    /**
     * 注册中心地址
     */
    private String address = "http://localhost:2381";

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接超时时间
     */
    private Long connectTimeout = 5000L;
}
