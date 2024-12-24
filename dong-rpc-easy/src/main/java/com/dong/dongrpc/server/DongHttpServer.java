package com.dong.dongrpc.server;

/**
 * http服务器接口
 */
public interface DongHttpServer {

    /**
     * 启动服务器
     */
    void doStart(int port);

}
