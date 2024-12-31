package com.dong.dongrpc.fault.tolerant;

import com.dong.dongrpc.model.RpcResponse;

import java.util.Map;

/**
 * 容错机制策略
 */
public interface TolerantStrategy {

    /**
     * 容错处理
     *
     * 当发生某个异常的时候，将当前状态的资源以及发生的异常传递进来进行容错处理
     * @param context
     * @param e
     * @return
     */
    RpcResponse doTolerant(Map<String, Object> context, Exception e);

}
