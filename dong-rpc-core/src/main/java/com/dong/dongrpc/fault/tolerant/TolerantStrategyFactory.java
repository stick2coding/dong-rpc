package com.dong.dongrpc.fault.tolerant;

import com.dong.dongrpc.utils.SpiLoder;

/**
 * 容错机制工厂
 */
public class TolerantStrategyFactory {

    static {
        SpiLoder.load(TolerantStrategy.class);
    }

    public static TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailFastTolerantStrategy();

    public static TolerantStrategy getInstance(String key){
        return  SpiLoder.getInstance(TolerantStrategy.class, key);
    }
}
