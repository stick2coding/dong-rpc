package com.dong.dongrpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册器
 */
public class LocalRegistry {

    /**
     * 注册信息存储
     */
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 服务注册
     * @param serverName
     * @param implClass
     */
    public static void register(String serverName, Class<?> implClass){
        map.put(serverName, implClass);
    }

    /**
     * 获取服务
     * @param serviceName
     * @return
     */
    public static Class<?> get(String serviceName){
        return map.get(serviceName);
    }

    /**
     * 移出服务
     * @param serviceName
     */
    public static void remove(String serviceName){
        map.remove(serviceName);
    }




}
