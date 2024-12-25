package com.dong.dongrpc;

import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.constant.RpcConstant;
import com.dong.dongrpc.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * rpc 应用启动类
 * 存放项目全局要用到的配置
 * 双检锁单例模式
 *
 */
@Slf4j
public class RpcApplication {

    private static volatile RpcConfig rpcConfig;

    private static void init(RpcConfig newRpcConfig) {
        rpcConfig = newRpcConfig;
        log.info("初始化配置文件: {}", rpcConfig);
    }

    public static void init(){
        RpcConfig newRpcConfig;
        try {
            newRpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        } catch (Exception e) {
            log.error("加载配置文件失败", e);
            // 使用默认配置
            newRpcConfig = new RpcConfig();
        }
        init(newRpcConfig);
    }

    /**
     * 获取配置（单例模式）
     * @return
     */
    public static RpcConfig getRpcConfig(){
        if (rpcConfig == null) {
            synchronized (RpcApplication.class) {
                if (rpcConfig == null) {
                    init();
                }
            }
        }
        return rpcConfig;
    }




    public static void main( String[] args ) {
        System.out.println( "Hello World!" );
    }
}
