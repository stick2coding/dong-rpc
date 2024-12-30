package com.dong.dongrpc.fault.retry;

import com.dong.dongrpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试策略
 */
public interface RetryStrategy {


    /**
     * 执行重试
     * 参数是一个返回类型为RpcResponse的callable
     * @param callable
     * @return
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;


}
