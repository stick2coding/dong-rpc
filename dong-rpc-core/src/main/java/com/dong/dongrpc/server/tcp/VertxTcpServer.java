package com.dong.dongrpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;

/**
 * 之前用vertx实现的也是HTTP 服务端
 * 同样Vertx也支持tcp服务器
 */
public class VertxTcpServer implements TcpServer {


    private byte[] handleRequest(byte[] requestData){
        System.out.println(requestData.toString());
        // 这里应该要根据业务来实现具体的处理过程，处理请求数据同时返回responseData
        System.out.println("VertxTcpServer handleRequest");
        return "hello".getBytes();
    }

    /**
     * 启动tcp服务并绑定处理请求，监听端口
     * @param port
     */
    @Override
    public void doStart(int port) {

        // 创建一个vertx实例
        Vertx vertx = Vertx.vertx();

        // 创建TCP服务器
        NetServer netServer = vertx.createNetServer();

        // 使用自定义处理器
        netServer.connectHandler(new TcpServerHandler());
        // 处理请求
//        netServer.connectHandler(netSocket -> {
//            // 处理连接
//            netSocket.handler(buffer -> {
//                System.out.println("Received request: " + buffer.toString());
//                // 处理接收到的字节数组
//                byte[] requestData = buffer.getBytes();
//                // 解析处理
//                byte[] responseData = handleRequest(requestData);
//                // 返回响应
//                netSocket.write(Buffer.buffer(responseData));
//
//
//            });
//        });

        // 启动并监听端口
        netServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("TCP server started on port " + port);
            } else {
                System.out.println("Failed to start TCP server: " + result.cause());
            }
        });
    }

    public static void main(String[] args) {
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(8888);
    }
}
