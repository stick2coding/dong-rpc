package com.dong.dongrpc.server.tcp;

import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.protocol.ProtocolMessage;
import com.dong.dongrpc.protocol.ProtocolMessageDecoder;
import com.dong.dongrpc.protocol.ProtocolMessageEncoder;
import com.dong.dongrpc.protocol.ProtocolMessageTypeEnum;
import com.dong.dongrpc.registry.LocalRegistry;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * tcp协议下的请求处理器
 */
public class TcpServerHandler implements Handler<NetSocket> {

    /**
     * 入口,从socket进行处理
     * @param netSocket
     */
    @Override
    public void handle(NetSocket netSocket) {

        //先进入装饰器处理buffer,然后拿到处理后的buffer，再进入自定义buffer处理器进行处理
        TcpBufferHandlerWrapper handlerWrapper = new TcpBufferHandlerWrapper(buffer -> {
            System.out.println("Received request from client: " + buffer.toString());
            // 将请求数据转成java对象
            ProtocolMessage<RpcRequest> protocolMessage;
            try {
                protocolMessage = (ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            }catch (IOException e) {
                throw new RuntimeException("解码错误。。。");
            }
            RpcRequest rpcRequest = protocolMessage.getBody();

            // 处理请求（这里和HTTP的基本差不多）
            // 拿到实现类，通过反射调用，然后返回数据
            // 构建rpc响应结果
            RpcResponse rpcResponse = new RpcResponse();

            // 开始调用
            try {
                // 找到实现类
                System.out.println("本次请求的服务名为：" + rpcRequest.getServiceName());
                System.out.println("本次请求的接口名为：" + rpcRequest.getIntfaceName());
                Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
                // 拿到对应的方法
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                System.out.println("本次请求的方法名为：" + rpcRequest.getMethodName());
                // 传入参数，通过反射的机制执行方法获取返回结果
                Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("success");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 返回响应，需要将数据编码成buffer写入到socket
            ProtocolMessage.Header header = protocolMessage.getHeader();
            // 更新header为返回类型
            header.setType((byte)ProtocolMessageTypeEnum.RESPONSE.getTypeCode());
            // 组装返回结果
            ProtocolMessage<RpcResponse> response = new ProtocolMessage<RpcResponse>(header, rpcResponse);
            // 编码
            try {
                Buffer resultBuffer = ProtocolMessageEncoder.encode(response);
                netSocket.write(resultBuffer);
            } catch (IOException e) {
                throw new RuntimeException("返回数据编码错误。。。");
            }
            System.out.println("本次请求执行完毕...");
        });

        netSocket.handler(handlerWrapper);
    }
}
