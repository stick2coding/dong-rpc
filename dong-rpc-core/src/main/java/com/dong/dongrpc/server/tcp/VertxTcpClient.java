package com.dong.dongrpc.server.tcp;

import cn.hutool.core.util.IdUtil;
import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.protocol.*;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * tcp客户端
 */
public class VertxTcpClient {

    /**
     * 发送tcp请求
     * @param rpcRequest
     * @param selectServiceMetaInfo
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static RpcResponse doTcpRequest(RpcRequest rpcRequest, ServiceMetaInfo selectServiceMetaInfo) throws ExecutionException, InterruptedException {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 创建实例
        Vertx vertx = Vertx.vertx();

        // 创建一个客户端
        NetClient netClient = vertx.createNetClient();

        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        // 连接服务端并发送数据，处理返回数据
        netClient.connect(
                selectServiceMetaInfo.getServicePort(),
                selectServiceMetaInfo.getServiceHost(),
                result -> {
                    // 连接成功
                    if (result.succeeded()){
                        System.out.println("Connected to server");
                        //创建socket
                        NetSocket socket = result.result();
                        // 构造消息
                        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
                        ProtocolMessage.Header header = new ProtocolMessage.Header();
                        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
                        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
                        header.setSerializer((byte) ProtocolMessageSerializerEnum.getByText(rpcConfig.getSerializerType()).getCode());
                        // 类型
                        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getTypeCode());
                        // id
                        header.setRequestId(IdUtil.getSnowflakeNextId());
                        // header
                        protocolMessage.setHeader(header);
                        protocolMessage.setBody(rpcRequest);
                        // 编码
                        try {
                            Buffer resultBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                            socket.write(resultBuffer);
                        } catch (IOException e) {
                            throw new RuntimeException("请求数据编码错误。。。");
                        }

                        // 等待接收响应 这里也要用wrapper增强
                        TcpBufferHandlerWrapper handlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
                            try {
                                // 解码
                                ProtocolMessage<RpcResponse> responseProtocolMessage =
                                        (ProtocolMessage<RpcResponse>) ProtocolMessageDecoder.decode(buffer);

                                // 响应
                                responseFuture.complete(responseProtocolMessage.getBody());
                            }catch (IOException e){
                                throw new RuntimeException("返回数据编码错误。。。");
                            }
                        });
                        socket.handler(handlerWrapper);
                    }else {
                        System.out.println("连接失败");
                        // 这里抛出异常，是为了可以让重试策略捕获到异常，便于进行重试
                        responseFuture.completeExceptionally(new RuntimeException("连接失败"));
                        return;
                    }
                });

        RpcResponse rpcResponse = responseFuture.get();
        // 关闭连接
        netClient.close();
        return rpcResponse;

    }

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
