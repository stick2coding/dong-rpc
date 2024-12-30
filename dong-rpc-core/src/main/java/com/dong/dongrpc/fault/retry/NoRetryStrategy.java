package com.dong.dongrpc.fault.retry;

import com.dong.dongrpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 不重试
 * 直接执行
 */
public class NoRetryStrategy implements RetryStrategy{
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
