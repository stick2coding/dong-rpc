package com.dong.dongrpc.server.tcp;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

/**
 * tcp客户端
 */
public class VertxTcpClient {

    public void start(){

        // 创建vertx客户端
        Vertx vertx = Vertx.vertx();

        // 连接服务端
        vertx.createNetClient().connect(8888, "localhost", result -> {
            // 连接成功
            if (result.succeeded()) {
                System.out.println("Connected to server");
                // 拿到成功后的socket连接
                NetSocket netSocket = result.result();
                // 给服务端发送数据
                netSocket.write(Buffer.buffer("hello server"));

                // 接收响应
                netSocket.handler(buffer -> {
                    System.out.println("Received response from server: " + buffer.toString());
                });

            }else {
                System.out.println("Failed to connect to server: " + result.cause());
            }
        });

    }

    public static void main(String[] args) {
        new VertxTcpClient().start();
    }

}
