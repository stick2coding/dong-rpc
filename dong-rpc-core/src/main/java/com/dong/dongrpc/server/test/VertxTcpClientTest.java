package com.dong.dongrpc.server.test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.BufferedReader;

/**
 * tcp客户端
 */
public class VertxTcpClientTest {

    public void start(){

        // 创建vertx客户端
        Vertx vertx = Vertx.vertx();

        // 连接服务端
        vertx.createNetClient().connect(8888, "localhost", this::testDataTransport);

    }

    /**
     * 测试一下粘包和半包的情况
     * 连续发1000次数据
     * @param result
     */
    private void testDataTransport(AsyncResult<NetSocket> result) {
        if (result.succeeded()) {
            System.out.println("Connected to TCP server");
            io.vertx.core.net.NetSocket socket = result.result();
            for (int i = 0; i < 1000; i++) {
                System.out.println("Send data to server: " + i);
                // 发送数据（这里简单自定义下消息格式，头+体）
                Buffer buffer = Buffer.buffer();
                // length 56
                String str = "Hello, server!Hello, server!Hello, server!Hello, server!";

                buffer.appendInt(0);
                buffer.appendInt(str.getBytes().length);
                buffer.appendBytes(str.getBytes());
                socket.write(buffer);
                System.out.println("Send data to server: " + buffer.toString());
            }
            // 接收响应
            socket.handler(buffer -> {
                System.out.println("Received response from server: " + buffer.toString());
            });
        } else {
            System.err.println("Failed to connect to TCP server");
        }
    }

    private void testInit(AsyncResult<NetSocket> result) {
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
    }

    public static void main(String[] args) {
        new VertxTcpClientTest().start();
    }

}

