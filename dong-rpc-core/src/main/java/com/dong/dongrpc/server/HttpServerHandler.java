package com.dong.dongrpc.server;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.registry.LocalRegistry;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.SerializerFactory;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * http请求处理器
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    /**
     * 自定义请求处理器
     * @param request
     */
    @Override
    public void handle(HttpServerRequest request) {
        // 指定序列化器（改用工厂模式）
//        final DongSerializer serializer = new JdkSerializer();
        final DongSerializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializerType());

        System.out.println("请求进入自定义请求处理器 dongHttpServerHandler，开始处理...");
        // 记录日志
        System.out.println("请求来了...httpMethod:" + request.method() + ";uri:" + request.uri());

        // 异步处理请求
        request.bodyHandler(body -> {
            // 获取到请求中的内容
            byte[] bytes = body.getBytes();

            // 将请求体反序列化为想要的rpc请求对象
            RpcRequest rpcRequest = null;
            try {
                // 反序列化请求参数
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // 构建rpc响应结果
            RpcResponse rpcResponse = new RpcResponse();
            
            // 如果请求为null，直接返回
            if (rpcRequest == null){
                rpcResponse.setMessage("requset is null!");
                // 构建返回体
                doResponse(request, rpcResponse, serializer);
                return;
            }

            // 开始调用
            try {
                // 找到实现类
                System.out.println("本次请求的服务名为：" + rpcRequest.getServiceName());
                System.out.println("本次请求的接口名为：" + rpcRequest.getIntfaceName());
                Class<?> implClass = LocalRegistry.get(rpcRequest.getIntfaceName());
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

            // 响应结果
            doResponse(request, rpcResponse, serializer);
            System.out.println("本次请求执行完毕...");
        });
    }

    /**
     * 序列化返回内容，并返回
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    private void doResponse(HttpServerRequest request,
                            RpcResponse rpcResponse, DongSerializer serializer) {
        // 返回头
        HttpServerResponse httpServerResponse = request.response()
                .putHeader("content-type", "application/json");

        // 序列化
        try{
            byte[] serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        }catch (IOException e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
