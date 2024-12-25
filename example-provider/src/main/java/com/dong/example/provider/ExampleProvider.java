package com.dong.example.provider;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.server.DongHttpServer;
import com.dong.dongrpc.server.VertxDongHttpServer;
import com.dong.example.common.service.UserService;
import com.dong.example.provider.service.impl.UserServiceImpl;

public class ExampleProvider {

    public static void main( String[] args ) {
        // 提供服务
        System.out.println( "Hello! provider start success!" );

        // 将自身的服务注册到框架的本地注册器中
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 服务提供者引入RPC框架，然后启动rpc框架中的web服务器
        DongHttpServer dongHttpServer = new VertxDongHttpServer();
        System.out.println("当前web服务器端口：" + RpcApplication.getRpcConfig().getServerPort());
        dongHttpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }

}
