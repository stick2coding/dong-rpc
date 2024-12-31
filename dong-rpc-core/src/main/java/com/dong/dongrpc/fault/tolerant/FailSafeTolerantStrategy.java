package com.dong.dongrpc.fault.tolerant;

import com.dong.dongrpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 静默异常，不处理
 */
@Slf4j
public class FailSafeTolerantStrategy implements TolerantStrategy{
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("容错[fail-safe]静默异常不处理:", e);
        return new RpcResponse();
    }
}
