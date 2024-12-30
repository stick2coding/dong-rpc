package com.dong.dongrpc.fault.retry;

import com.dong.dongrpc.utils.SpiLoder;

/**
 * 重试策略工厂
 */
public class RetryStrategyFactory {

    static {
        SpiLoder.load(RetryStrategy.class);
    }


    /**
     * 默认的重试策略
     */
    public static RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取重试策略
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key) {
        return SpiLoder.getInstance(RetryStrategy.class, key);
    }


}
