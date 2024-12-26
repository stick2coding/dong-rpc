package com.dong.dongrpc.registry;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EtcdDongRegistry implements DongRegistry{

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();


    private Client client;

    private KV kvClient;

    /**
     * etcd根路径
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 注册中心服务缓存（单服务）
     */
    private final RegistrySingleServiceCache registrySingleServiceCache =  new RegistrySingleServiceCache();

    /**
     * 注册中心服务缓存（多服务）
     */
    private final RegistryMultiServiceCache registryMultiServiceCache =  new RegistryMultiServiceCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();




    /**
     * 心跳
     * 需要实现一个定时任务，定期的维护集合内的节点
     * 给节点key进行续期
     * 利用hutool的cronUtil 实现
     * 实现后，将定时任务的入口放在注册中心初始化的地方
     */
    @Override
    public void heartbeat() {

        // 定义一个定时任务，指定循环间隔时间，和执行逻辑
        CronUtil.schedule("*/10 * * * * ?", new Task() {
            @Override
            public void execute() {
                log.info("心跳维护开始...");
                for (String key : localRegisterNodeKeySet) {
                    try {
                        // 先拿出节点
                        List<KeyValue> keyValues = kvClient
                                .get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get().getKvs();
                        // 判断是否过期，过期就是拿出来的节点列表是空的，因为过期就会自动被清理
                        if (CollUtil.isEmpty(keyValues)) {
                            log.info("当前节点已全部过期，无法续费，需要重新启动服务来注册...{}", key);
                            // 因为过期被自动清理，就无法续期，需要重新启动来注册
                            continue;
                        }
                        // 没有过期，就进行续期
                        // 拿到节点
                        KeyValue keyValue = keyValues.get(0);
                        // 拿出原信息，重新注册一遍？todo，无法直接给节点续费？
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        // 重新注册一遍
                        log.info("节点未过期，续期中...{}", serviceMetaInfo.getServiceName());
                        register(serviceMetaInfo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //支持秒级定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();

    }

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

        // 同时，开启心跳维护，这部分实际上是给服务提供者用到
        heartbeat();
        log.info("开启心跳维护...");
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

        // 注册完成后，将节点信息存入到集合中
        localRegisterNodeKeySet.add(registryServiceNodeKey);
    }

    /**
     * 注销节点，就是把key删掉
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        // 移出节点的时候，注意添加根节点
        String registryServiceNodeKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registryServiceNodeKey, StandardCharsets.UTF_8));

        // 已经主动注销的节点，就需要从集合中删除
        localRegisterNodeKeySet.remove(registryServiceNodeKey);
    }

    /**
     * 服务发现，就是以服务名称做为前缀，向下搜索节点
     * 这里增加改用本地缓存寻找服务列表
     * serviceKey = serviceName + serviceVersion
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 前缀搜索
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        // 如果只有一个服务，使用单服务缓存
//        List<ServiceMetaInfo> cachedServiceList = registrySingleServiceCache.readCache();
        // 多服务
        List<ServiceMetaInfo> cachedServiceList = registryMultiServiceCache.readCache(serviceKey);
        if (CollUtil.isNotEmpty(cachedServiceList)){
            log.info("从本地缓存中获取服务列表..." + serviceKey);
            return cachedServiceList;
        }

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
                        // 监听key，要来调用服务发现的都是服务消费者，服务消费者想要将注册信息缓存到本地
                        // 那么就需要对注册中心的key进行监听，如果key发生变化要及时更新本地
                        String keyStr = kv.getKey().toString(StandardCharsets.UTF_8);
                        watch(keyStr);

                        String value = kv.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());

            // 初次获取到数据后，放入本地
            //registrySingleServiceCache.writeCache(result);
            registryMultiServiceCache.writeCache(serviceKey, result);
            return result;
        } catch (Exception e){
            throw new RuntimeException("获取服务列表失败...");
        }
    }

    @Override
    public void watch(String serviceKey) {
        // 创建一个监听客户端
        Watch watchClient = client.getWatchClient();
        // 将节点key加入监听列表，判断是否已经在监听，set集合放入已有元素，返回0，不在返回1
        Boolean isNewWatchKey = watchingKeySet.add(serviceKey);
        // 如果是新监听的key
        if (isNewWatchKey){
            log.info("新监听的key:{}", serviceKey);
            // watch方法，第一个参数是key,第二个是拿到监听到的事件
            watchClient.watch(
                    ByteSequence.from(serviceKey, StandardCharsets.UTF_8), watchResponse -> {
                        // 循环监听到的事件
                        for (WatchEvent event : watchResponse.getEvents()){
                            switch (event.getEventType()){
                                case DELETE:
                                    // 同步清理本地
                                    //registrySingleServiceCache.clearCache();
                                    registryMultiServiceCache.clearCache(serviceKey);
                                    break;
                                case PUT:
                                    // 如果有新增也删掉，下次获取最新的
                                    registryMultiServiceCache.clearCache(serviceKey);
                                default:
                                    break;
                            }
                        }
                    }
            );
        }

    }

    /**
     * 服务停止后，清理资源
     */
    @Override
    public void destroy() {
        System.out.println("当前节点下线...");

        // 主动移出所有节点
        for (String key : localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
            } catch (Exception e) {
                throw new RuntimeException("释放资源失败..." + key);
            }
        }
        // 释放资源
        if (client != null){
            client.close();
        }
        if (kvClient != null){
            kvClient.close();
        }

    }
}
