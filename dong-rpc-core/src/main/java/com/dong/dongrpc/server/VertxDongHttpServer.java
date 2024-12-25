package com.dong.dongrpc.server;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;

/**
 * 使用vertx来实现http服务器
 */
public class VertxDongHttpServer implements DongHttpServer {
    @Override
    public void doStart(int port) {
        // 创建实例
        Vertx vertx = Vertx.vertx();

        // 创建HTTP服务器
        HttpServer httpServer = vertx.createHttpServer();

        // 配置一个请求处理器
//        httpServer.requestHandler(req -> {
//           // 请求处理器 处理请求
//            System.out.println("请求来了...method:" + req.method() + " uri:" + req.uri());
//
//            // 回复相应
//            req.response()
//                    .putHeader("content-type", "text/plain")
//                    .end("Hello World! from vert.x http server");
//        });
        // 更换为自定义的请求处理器
        httpServer.requestHandler(new DongHttpServerHandler());

        // 启动服务器并监听端口
        httpServer.listen(port, result -> {
            if (result.succeeded()) {
                System.out.println("HTTP server started on port " + port);
            } else {
                System.out.println("HTTP server failed to start on port " + port);
            }
        });

    }
}
