package com.dong.dongrpc.server.test;

import com.dong.dongrpc.server.TcpServer;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetServer;
import io.vertx.core.parsetools.RecordParser;

/**
 * 之前用vertx实现的也是HTTP 服务端
 * 同样Vertx也支持tcp服务器
 */
public class VertxTcpServerTest implements TcpServer {


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

        // 处理请求
        netServer.connectHandler(netSocket -> {

            //初步解决粘包
            RecordParser parser = getRecordParser();

            // 有一个问题，就是实际发数据的时候，每次的数据长度是不一样的，是动态长度
            // 如何解决
            //RecordParser parser = getDynamicRecordParser();

            netSocket.handler(parser);
        });



//            // 处理连接
//            netSocket.handler(buffer -> {
//                System.out.println("Received request: " + buffer.toString());
//
//                //测试粘包和半包的情况
//                //testDataTransport(buffer);
//
////                // 处理接收到的字节数组
////                byte[] requestData = buffer.getBytes();
////
////                // 解析处理
////                byte[] responseData = handleRequest(requestData);
////                // 返回响应
////                netSocket.write(Buffer.buffer(responseData));
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

    /**
     * 动态解决粘包问题
     * 我们可以按照自定义消息结构来解决
     * 由于数据分为两部分，头 + 体
     * 一般来说，头都是固定的长度，可以先读固定长度，然后再根据实际的数据长度读
     * @return
     */
    private RecordParser getDynamicRecordParser() {
        int headLength = 8;
        // 先取头部数据（根据测试方法中的自定义消息结构，2个int,就是8）
        RecordParser parser = RecordParser.newFixed(headLength);

        parser.setOutput(new Handler<Buffer>() {
            // 初始化
            int size = -1;
            // 接收数据（如何去一次读完整的数据）
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (-1 == size){
                    // 第一次读，先读数据长度
                    size = buffer.getInt(4);
                    System.out.println("size = " + size);
                    // 修正下次读的长度
                    parser.fixedSizeMode(size);
                    // 这里就变成头的数据
                    resultBuffer.appendBuffer(buffer);
                    System.out.println(buffer.toString());
                } else {
                    // 第二次读就读到了实际数据
                    resultBuffer.appendBuffer(buffer);
                    System.out.println(resultBuffer.toString());
                    // 读完之后，重置下次读数据的长度（就是请求头的长度）
                    parser.fixedSizeMode(headLength);
                    size = -1;
                    // 重置缓冲区
                    resultBuffer = Buffer.buffer();


                }
            }
        });
        return parser;

    }

    /**
     * 静态解决粘包问题
     * @return
     */
    private static RecordParser getRecordParser() {
        // 解决粘包和半包
        String testMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
        int messageLength = testMessage.getBytes().length;
        // 构建parser（读固定长度）这里先将handler包起来，然后通过parser将handler放到socket中
        RecordParser parser = RecordParser.newFixed(messageLength);
        Buffer resultBuffer = Buffer.buffer();
        parser.setOutput(buffer -> {
            String str = new String(buffer.getBytes());
            System.out.println(str);
            if (testMessage.equals(str)) {
                resultBuffer.appendBuffer(buffer);
                System.out.println("good" + buffer.toString());
            }
        });
        return parser;
    }

    /**
     * 测试粘包半包的情况
     * @param buffer
     */
    private static void testDataTransport(Buffer buffer) {
        String testMessage = "Hello, server!Hello, server!Hello, server!Hello, server!";
        int messageLength = testMessage.getBytes().length;
        if (buffer.getBytes().length < messageLength) {
            System.out.println("半包, length = " + buffer.getBytes().length);
            return;
        }
        if (buffer.getBytes().length > messageLength) {
            System.out.println("粘包, length = " + buffer.getBytes().length);
            return;
        }
        String str = new String(buffer.getBytes(0, messageLength));
        System.out.println(str);
        if (testMessage.equals(str)) {
            System.out.println("good");
        }
    }

    public static void main(String[] args) {
        VertxTcpServerTest vertxTcpServer = new VertxTcpServerTest();
        vertxTcpServer.doStart(8888);
    }
}

