package com.dong.dongrpc.model;

import cn.hutool.core.util.StrUtil;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.constant.RpcConstant;
import lombok.Data;

/**
 * 服务元信息
 */
@Data
public class ServiceMetaInfo {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本
     */
    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;

    /**
     * 服务地址
     */
    private String serviceHost;

    /**
     * 服务端口
     */
    private Integer servicePort;

    /**
     * 服务分组
     */
    private String serviceGroup = "default";


    /**
     * 获取服务节点的key
     * @return
     */
    public String getServiceKey() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取二级实例节点的key
     * @return
     */
    public String getServiceNodeKey() {
        return String.format("%s/%s:%s", getServiceKey(), serviceHost, servicePort);
    }

    /**
     * 获取服务地址
     * @return
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")){
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }

}
