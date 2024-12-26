package com.dong.example.provider.config;

import lombok.Data;

@Data
public class ProviderConfig {

    /**
     * 当前服务提供者的名字
     */
    private String serviceName;

    /**
     * 对外rpc接口的路径
     */
    private String dongRpcServicePath;

}
