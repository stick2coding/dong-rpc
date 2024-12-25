package com.dong.dongrpc.serializer;

import com.dong.dongrpc.utils.SpiLoder;

import java.util.HashMap;
import java.util.Map;


/**
 * 序列化工厂
 * 用于获取序列化器
 */
public class SerializerFactory {

    /**
     * 映射表(在使用spi机制后，这里可以使用spiloaer来进行初始化)
     */
//    private static final Map<String, DongSerializer> KEY_SERIALIZER_MAP = new HashMap<String, DongSerializer>(){
//        {
//            put(SerializerKeys.JSON_SERIALIZER, new JsonSerializer());
//            put(SerializerKeys.HESSIAN_SERIALIZER, new HessianSerializer());
//            put(SerializerKeys.KYRO_SERIALIZER, new KyroSerializer());
//            put(SerializerKeys.JDK_SERIALIZER, new JdkSerializer());
//        }
//    };
    //todo 这里可以取消，然后再获取实例的时候，通过双检锁判断是否存在，不存在再load，懒加载
    static {
        SpiLoder.load(DongSerializer.class);
    }

    /**
     * 默认值
     */
//    public static final DongSerializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get(SerializerKeys.JDK_SERIALIZER);
    public static final DongSerializer DEFAULT_SERIALIZER = new JdkSerializer();


    /**
     * 获取实例
     */
    public static DongSerializer getInstance(String key){
//        return KEY_SERIALIZER_MAP.get(key);
        return SpiLoder.getInstance(DongSerializer.class, key);
    }
}
