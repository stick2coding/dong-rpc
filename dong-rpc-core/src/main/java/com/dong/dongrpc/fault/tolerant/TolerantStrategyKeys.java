package com.dong.dongrpc.fault.tolerant;

/**
 * 容错策略键
 */
public interface TolerantStrategyKeys {

    String FAIL_OVER = "failOver";

    String FAIL_FAST = "failFast";

    String FAIL_SAFE = "failSafe";

    String FAIL_BACK = "failBack";

}
