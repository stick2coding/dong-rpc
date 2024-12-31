package com.dong.dongrpc.bootstrap;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.model.ServiceRegisterInfo;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.registry.RegistryFactory;
import com.dong.dongrpc.server.tcp.TcpServer;
import com.dong.dongrpc.server.tcp.VertxTcpServer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

public class ConsumerBootstrap {


    public static void init(){
        // 提供服务
        System.out.println( "Hello! provider starting!" );
        // 初始化RPC框架
        RpcApplication.init();

    }

}
