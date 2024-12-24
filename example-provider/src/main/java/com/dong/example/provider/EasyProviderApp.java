package com.dong.example.provider;

import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.server.DongHttpServer;
import com.dong.dongrpc.server.VertxDongHttpServer;
import com.dong.example.common.service.UserService;
import com.dong.example.provider.service.impl.UserServiceImpl;

/**
 * Hello world!
 *
 */
public class EasyProviderApp
{
    public static void main( String[] args ) {
        // 提供服务
        System.out.println( "Hello! provider start success!" );

        // 将自身的服务注册到框架的本地注册器中
        LocalRegistry.register(UserService.class.getName(), UserServiceImpl.class);

        // 服务提供者引入RPC框架，然后启动rpc框架中的web服务器
        DongHttpServer dongHttpServer = new VertxDongHttpServer();
        dongHttpServer.doStart(8080);

    }
}
