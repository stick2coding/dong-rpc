package com.dong.dongrpc.server.tcp;

/**
 * TCP服务器接口（只是单纯和httpserver文件区分一下，实际开发可以用一个文件表示）
 */
public interface TcpServer {

    /**
     * 启动服务器
     */
    void doStart(int port);

}
