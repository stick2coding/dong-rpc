package com.dong.dongrpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.JdkSerializer;
import com.dong.dongrpc.serializer.SerializerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 服务代理（jdk动态代理）
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器（这里改用工厂获取）
        DongSerializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializerType());

        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        try {
            // 序列化请求体
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 接收返回值
            byte[] resultBytes;
            // todo 这里的地址应该要依赖服务注册发现机制来实现，不能硬编码
            String postUrl = "http://" + RpcApplication.getRpcConfig().getServerHost() + ":" + RpcApplication.getRpcConfig().getServerPort() + "/user";
            try (HttpResponse httpResponse = HttpRequest.post(postUrl)
                    .body(bodyBytes).execute()){
                resultBytes = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(resultBytes, RpcResponse.class);
            return rpcResponse.getData();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
