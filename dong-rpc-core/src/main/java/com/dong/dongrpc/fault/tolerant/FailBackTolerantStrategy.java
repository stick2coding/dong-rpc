package com.dong.dongrpc.fault.tolerant;

import com.dong.dongrpc.model.RpcResponse;

import java.util.Map;

/**
 * 故障恢复
 *
 * 这里可以说是一种降级策略，如果出现故障的时候，执行更加稳定的操作使得请求看起来正常
 *
 */
public class FailBackTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        return null;
    }
}
