package com.dong.dongrpc.fault.tolerant;

import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.fault.retry.RetryStrategy;
import com.dong.dongrpc.fault.retry.RetryStrategyFactory;
import com.dong.dongrpc.model.RpcRequest;
import com.dong.dongrpc.model.RpcResponse;
import com.dong.dongrpc.model.ServiceMetaInfo;
import com.dong.dongrpc.server.tcp.VertxTcpClient;
import com.dong.dongrpc.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 故障转移
 *
 * 在微服务中，如果当前请求节点出现问题，可以将请求转移到其他节点
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 获取上下文
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        ServiceMetaInfo selectServiceMetaInfo = (ServiceMetaInfo) context.get("selectServiceMetaInfo");
        List<ServiceMetaInfo> serviceMetaInfoList = (List<ServiceMetaInfo>) context.get("ServiceMetaInfoList");

        //移出故障节点
        serviceMetaInfoList.remove(selectServiceMetaInfo);

        // 重新选择节点
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("interfaceName", rpcRequest.getMethodName());
        ServiceMetaInfo newSelectServiceMetaInfo = RequestUtils.serviceSelect(requestParams, serviceMetaInfoList);
        RpcResponse rpcResponse = new RpcResponse();
        try {
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(RpcApplication.getRpcConfig().getRetryStrategyType());
            // 发送TCP请求（增加重试）
            rpcResponse = retryStrategy.doRetry(
                    () -> VertxTcpClient.doTcpRequest(rpcRequest, newSelectServiceMetaInfo));
            return rpcResponse;
        } catch (Exception ex) {
            // 如果还是出现异常，当前节点也不可用，移出当前节点，继续后继续选择
            log.error("service node is down" + selectServiceMetaInfo.getServiceAddress());
            if (serviceMetaInfoList.size() > 1){
                // 继续容错
                // 出现异常的节点
                context.put("selectServiceMetaInfo", newSelectServiceMetaInfo);
                context.put("ServiceMetaInfoList", serviceMetaInfoList);
                this.doTolerant(context, ex);
            }
        }

        // 如果没有返回，说明全部异常，抛出异常
        throw new RuntimeException("all service node is down");
    }

}
