package com.dong.dongrpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import com.dong.dongrpc.config.RegistryConfig;
import com.dong.dongrpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * zk注册中心
 */
@Slf4j
public class ZkDongRegistry implements DongRegistry{

    /**
     * zk客户端
     */
    private CuratorFramework client;

    /**
     * 服务发现
     */
    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * zk的根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc/zk";

    /**
     * 注册中心服务缓存（多服务）
     */
    private final RegistryMultiServiceCache registryMultiServiceCache =  new RegistryMultiServiceCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();


    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    @Override
    public void init(RegistryConfig registryConfig) {
        // 构建一个client实例
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getConnectTimeout()), 3))
                .build();

        // 服务发现，zk客户端工具包有的
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();

        // 启动
        try {
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException("zk连接失败...");
        }

    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 将节点放到zk
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));

        // 添加节点到本地
        String registryServiceNodeKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.add(registryServiceNodeKey);
    }

    /**
     * 构建zk节点
     * @param serviceMetaInfo
     * @return
     */
    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
        // 拼一下地址
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try {
            return ServiceInstance.<ServiceMetaInfo>builder()
                    .id(serviceAddress)
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .payload(serviceMetaInfo)
                    .build();
        }catch (Exception e){
            throw new RuntimeException("构建zk节点失败...", e);
        }

    }

    @Override
    public void heartbeat() {
        // zk的临时节点，当服务停止后，节点自动就没了

    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        try {
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        }catch (Exception e){
            throw new RuntimeException("注销节点失败...", e);
        }

        // 从本地移除
        String registryServiceNodeKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.remove(registryServiceNodeKey);
    }

    /**
     * 当提供接口的服务下线后，主动移除该服务注册的所有节点
     */
    @Override
    public void destroy() {
        log.info("当前节点下线。。。");
        // 下线，也可以不用手动处理
        for (String key : localRegisterNodeKeySet){
            try {
                client.delete().guaranteed().forPath(key);
            }catch (Exception e){
                throw new RuntimeException("节点删除失败..." + key);
            }
        }

        // 释放资源
        if (client != null){
            client.close();
        }

    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存获取
        List<ServiceMetaInfo> cachedServiceList = registryMultiServiceCache.readCache(serviceKey);
        if (CollUtil.isNotEmpty(cachedServiceList)){
            log.info("从本地缓存中获取服务列表..." + serviceKey);
            return cachedServiceList;
        }

        try{
            // 查服务
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstanceList = serviceDiscovery.queryForInstances(serviceKey);

            // 解析
            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstanceList.stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList());
            // 写入缓存
            registryMultiServiceCache.writeCache(serviceKey, serviceMetaInfoList);
            // 返回
            return serviceMetaInfoList;

        }catch (Exception e){
            throw new RuntimeException("获取服务列表失败...");
        }
    }

    @Override
    public void watch(String serviceKey) {
        String watchingKey = ZK_ROOT_PATH + "/" + serviceKey;
        // 将节点key加入监听列表，判断是否已经在监听，set集合放入已有元素，返回0，不在返回1
        Boolean isNewWatchKey = watchingKeySet.add(watchingKey);
        // 如果是新监听的key
        if (isNewWatchKey) {
            log.info("新监听的key:{}", serviceKey);
            CuratorCache curatorCache = CuratorCache.build(client, watchingKey);
            curatorCache.start();
            curatorCache.listenable().addListener(
                    CuratorCacheListener.builder()
                            .forDeletes(childData -> registryMultiServiceCache.clearCache(serviceKey))
                            .forChanges((oldNode, newNode) -> registryMultiServiceCache.clearCache(serviceKey))
                            .build()

            );
        }
    }
}
