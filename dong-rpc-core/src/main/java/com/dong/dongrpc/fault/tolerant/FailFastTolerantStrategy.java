package com.dong.dongrpc.fault.tolerant;

import com.dong.dongrpc.model.RpcResponse;

import java.util.Map;

/**
 * 快速失败
 *
 * 将异常直接抛出，交给上层进行处理
 */
public class FailFastTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("容错处理[fail-fast]服务报错:", e);
    }
}
