package com.dong.dongrpc.fault.retry;

import com.dong.dongrpc.model.RpcResponse;
import com.github.rholder.retry.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间重试
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy{


    /**
     * 使用Guava-Retrying
     * @param callable
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {

        // 构建一个重试策略器
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class) // 遇到异常进行重试（条件）
                .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS)) //等待时间
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)) //停止：重试次数
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数：" + attempt.getAttemptNumber());
                    }
                }) // 重试监听器，每次执行时打印日志
                .build();

        // 通过重试监听器去执行
        return retryer.call(callable);

    }
}
