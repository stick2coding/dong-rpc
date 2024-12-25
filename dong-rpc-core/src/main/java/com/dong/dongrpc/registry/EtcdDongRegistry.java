package com.dong.dongrpc.registry;


import cn.hutool.json.JSONUtil;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;

public class EtcdDongRegistry implements DongRegistry{

    private Client client;

    private KV kvClient;

    /**
     * etcd根路径
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";


    /**
     * 初始化
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        // 这里需要先连上注册中心
        this.client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getConnectTimeout()))
                .build();
        kvClient = client.getKVClient();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) {
        //创建lease 和 kv 客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个30s的租约
        long leaseId = leaseClient.grant(30).join().getID();

        // 节点中key就是名字，value就是节点的全部元信息
        // 拿到节点key
        String registryServiceNodeKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        // 键值对
        ByteSequence key = ByteSequence.from(registryServiceNodeKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值对和租约关联起来
        PutOption putOption = PutOption.newBuilder().withLeaseId(leaseId).build();
        kvClient.put(key, value, putOption);
    }

    /**
     * 注销节点，就是把key删掉
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        kvClient.delete(ByteSequence.from(ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey(), StandardCharsets.UTF_8));
    }

    /**
     * 服务发现，就是以服务名称做为前缀，向下搜索节点
     * @param serviceName
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceName) {
        // 前缀搜索
        String searchPrefix = ETCD_ROOT_PATH + serviceName + "/";

        try{
            //查询
            GetOption getOption = GetOption.newBuilder()
                    .isPrefix(true).build();
            List<KeyValue> keyValues = kvClient
                    .get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get().getKvs();

            //格式化信息
            List<ServiceMetaInfo> result = keyValues.stream()
                    .map(kv -> {
                        String value = kv.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());
            return result;
        } catch (Exception e){
            throw new RuntimeException("获取服务列表失败...");
        }
    }

    /**
     * 服务停止后，清理资源
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线...");
        // 释放资源
        if (client != null){
            client.close();
        }
        if (kvClient != null){
            kvClient.close();
        }

    }
}
