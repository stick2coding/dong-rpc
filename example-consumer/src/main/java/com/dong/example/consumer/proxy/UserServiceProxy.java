package com.dong.example.consumer.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.JdkSerializer;
import com.dong.example.common.model.User;
import com.dong.example.common.service.UserService;

/**
 * 静态代理类。代理对应的方法
 * 服务消费者中通过代理类去实现服务提供者的方法
 * 具体是为了消费者调用该方法时，通过代理类封装http请求调用rpc框架
 * 然后由框架再执行到服务提供者的方法
 */
public class UserServiceProxy implements UserService {

    /**
     *
     * @param user
     * @return
     */
    @Override
    public User getUser(User user) {

        // 指定序列化器
        DongSerializer serializer = new JdkSerializer();

        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class<?>[]{User.class})
                .args(new Object[]{user})
                .build();

        try {
            // 序列化请求体
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 接收返回值
            byte[] resultBytes;
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080/user")
                    .body(bodyBytes).execute()){
                resultBytes = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(resultBytes, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User getUserById(int userId) {
        // 指定序列化器
        DongSerializer serializer = new JdkSerializer();

        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUserById")
                .parameterTypes(new Class<?>[]{int.class})
                .args(new Object[]{userId})
                .build();

        try {
            // 序列化请求体
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 接收返回值
            byte[] resultBytes;
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080/user/id")
                    .body(bodyBytes).execute()){
                resultBytes = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserialize(resultBytes, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
