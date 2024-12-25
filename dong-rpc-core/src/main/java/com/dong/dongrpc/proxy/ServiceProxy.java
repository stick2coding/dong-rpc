package com.dong.dongrpc.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.constant.RpcConstant;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.RegistryFactory;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.JdkSerializer;
import com.dong.dongrpc.serializer.SerializerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

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
        // 接口名称
        String serviceName = method.getDeclaringClass().getName();
        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        try {
            // 序列化请求体
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            // 接收返回值
            byte[] resultBytes;

            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            // 引入注册中心
            // 拿到对应的注册中心实例
            DongRegistry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistryType());
            // 要查找的服务的基本元信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            // 搜索节点
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            // todo 负载均衡，这里先取第一个

            // 发送请求，先本地取，后续从注册中心取
//            String postUrl = "http://" + RpcApplication.getRpcConfig().getServerHost() + ":" + RpcApplication.getRpcConfig().getServerPort() + "/user";
            String postUrl = serviceMetaInfoList.get(0).getServiceAddress();
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
