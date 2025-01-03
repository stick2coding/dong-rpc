package com.dong.dongrpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.JdkSerializer;

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
        // 指定序列化器
        DongSerializer serializer = new JdkSerializer();

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
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080/user")
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
