package com.dong.dongrpc.registry;

import com.dong.dongrpc.utils.SpiLoder;

/**
 * 工厂模式
 * 复用SPI机制
 * 可以通过用户指定的方式来加载对应类型的注册中心
 */
public class RegistryFactory {

    static {
        SpiLoder.load(DongRegistry.class);
    }

    /**
     * 默认注册中心
     */
    private static final DongRegistry DEFAULT_REGISTRY = new EtcdDongRegistry();

    /**
     * 获取实例
     */
    public static DongRegistry getInstance(String key) {
        return SpiLoder.getInstance(DongRegistry.class, key);
    }


}
