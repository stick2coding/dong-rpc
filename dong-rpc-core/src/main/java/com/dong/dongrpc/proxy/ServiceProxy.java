package com.dong.dongrpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.annotation.DongRpcService;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.constant.RpcConstant;
import com.dong.dongrpc.fault.retry.RetryStrategy;
import com.dong.dongrpc.fault.retry.RetryStrategyFactory;
import com.dong.dongrpc.fault.tolerant.TolerantStrategy;
import com.dong.dongrpc.fault.tolerant.TolerantStrategyFactory;
import com.dong.dongrpc.loadbalancer.LoadBalancer;
import com.dong.dongrpc.loadbalancer.LoadBalancerFactory;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.protocol.*;
import com.dong.dongrpc.registry.DongRegistry;
import com.dong.dongrpc.registry.RegistryFactory;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.serializer.SerializerFactory;
import com.dong.dongrpc.server.tcp.TcpBufferHandlerWrapper;
import com.dong.dongrpc.server.tcp.VertxTcpClient;
import com.dong.dongrpc.utils.RequestUtils;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.security.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 服务代理（jdk动态代理）
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 通过注解的方式来获取到每个接口所属的服务名字
        Class<?> serviceClass = method.getDeclaringClass();
        //DongRpcService dongRpcService = serviceClass.getAnnotation(DongRpcService.class);
        // 接口名称
        String interfaceName = method.getDeclaringClass().getName();
        //String serviceName = dongRpcService.name();
        String serviceName = interfaceName;
        String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;
        // 发请求
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(interfaceName)
                //.intfaceName(interfaceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 负载均衡
        // 寻找节点
//        final ServiceMetaInfo selectServiceMetaInfo = providerServiceDiscoverAndSelect(requestParams, serviceName);
        ServiceMetaInfo seachServiceMetaInfo = new ServiceMetaInfo();
        seachServiceMetaInfo.setServiceName(serviceName);
        seachServiceMetaInfo.setServiceVersion(serviceVersion);
        List<ServiceMetaInfo> serviceMetaInfoList = RequestUtils.serviceDiscovery(seachServiceMetaInfo);
        if (CollUtil.isEmpty(serviceMetaInfoList)){
            throw new RuntimeException("没有发现服务节点");
        }
        // 选择节点
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("interfaceName", interfaceName);
        ServiceMetaInfo selectServiceMetaInfo = RequestUtils.serviceSelect(requestParams, serviceMetaInfoList);
        if (selectServiceMetaInfo == null){
            throw new RuntimeException("无法选择服务节点");
        }

        RpcResponse rpcResponse = new RpcResponse();
        try {
            // 发送HTTP请求
            //RpcResponse rpcResponse = httpRequest(rpcRequest, serializer, selectServiceMetaInfo);
            //RpcResponse rpcResponse = VertxTcpClient.doTcpRequest(rpcRequest, selectServiceMetaInfo);
            // 获取重试策略
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(RpcApplication.getRpcConfig().getRetryStrategyType());
            // 发送TCP请求（增加重试）
            rpcResponse = retryStrategy.doRetry(
                    () -> VertxTcpClient.doTcpRequest(rpcRequest, selectServiceMetaInfo));

        } catch (Exception e){
            //e.printStackTrace();
            // 出现异常之后，我们还要加入容错处理，先拿到容错策略
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(RpcApplication.getRpcConfig().getTolerantStrategyType());
            Map<String, Object> context = new HashMap<>();
            // 请求内容
            context.put("rpcRequest", rpcRequest);
            // 出现异常的节点
            context.put("selectServiceMetaInfo", selectServiceMetaInfo);
            context.put("ServiceMetaInfoList", serviceMetaInfoList);
            return tolerantStrategy.doTolerant(requestParams, e);
        }
        return rpcResponse.getData();
    }



    /**
     * 服务实例发现和选择
     * @param serviceName
     * @return
     */
    private ServiceMetaInfo providerServiceDiscoverAndSelect(Map<String, Object> obj, String serviceName) {
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 引入注册中心
        // 拿到对应的注册中心实例
        DongRegistry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistryType());
        // 要查找的服务的基本元信息
        ServiceMetaInfo seachServiceMetaInfo = new ServiceMetaInfo();
        seachServiceMetaInfo.setServiceName(serviceName);
        seachServiceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        // 搜索节点 key userService:1.0
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(seachServiceMetaInfo.getServiceKey());
        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancerType());
        ServiceMetaInfo selectServiceMetaInfo = loadBalancer.select(obj, serviceMetaInfoList);
        //ServiceMetaInfo selectServiceMetaInfo = serviceMetaInfoList.get(0);
        return selectServiceMetaInfo;
    }

    /**
     * 发送http请求
     * @param rpcRequest
     * @param selectServiceMetaInfo
     * @return
     * @throws IOException
     */
    private RpcResponse httpRequest(RpcRequest rpcRequest, ServiceMetaInfo selectServiceMetaInfo) throws IOException {
        // 指定序列化器（这里改用工厂获取）
        DongSerializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializerType());
        // 序列化请求体
        byte[] bodyBytes = serializer.serialize(rpcRequest);
        // 接收返回值
        byte[] resultBytes;

        //String postUrl = "http://" + RpcApplication.getRpcConfig().getServerHost() + ":" + RpcApplication.getRpcConfig().getServerPort() + "/user";
        String postUrl = selectServiceMetaInfo.getServiceAddress() + "/rpc/" + rpcRequest.getServiceName() + "/" + rpcRequest.getIntfaceName();
        System.out.println("postUrl:" + postUrl);
        try (HttpResponse httpResponse = HttpRequest.post(postUrl)
                .body(bodyBytes).execute()){
            resultBytes = httpResponse.bodyBytes();
        }
        RpcResponse rpcResponse = serializer.deserialize(resultBytes, RpcResponse.class);
        return rpcResponse;
    }
}
