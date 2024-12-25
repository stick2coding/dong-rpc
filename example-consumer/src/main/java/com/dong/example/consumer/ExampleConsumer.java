package com.dong.example.consumer;

import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.serializer.DongSerializer;
import com.dong.dongrpc.utils.ConfigUtils;
import com.dong.dongrpc.utils.SpiLoder;

import java.util.ServiceLoader;

public class ExampleConsumer {

    public static void main(String[] args) {

        RpcConfig config = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(config);

        DongSerializer serializer = null;
        ServiceLoader<DongSerializer> serviceLoader = ServiceLoader.load(DongSerializer.class);
        for (DongSerializer service : serviceLoader) {
            serializer = service;
        }
        SpiLoder.load(DongSerializer.class);


    }
}
