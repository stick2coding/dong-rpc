package com.dong.dongrpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRegisterInfo<T> {

    /**
     * 服务名称（对应的接口文件名称）
     */
    private String serviceName;

    /**
     * 实现类
     */
    private Class<? extends T> implClass;

}
