package com.dong.dongrpc.registry;

import com.dong.dongrpc.config.RegistryConfig;

import com.dong.dongrpc.model.ServiceMetaInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class DongRegistryTest {

    final DongRegistry dongRegistry = new EtcdDongRegistry();

    @Before
    public void testInit() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("http://localhost:2379");
        dongRegistry.init(registryConfig);
    }

    @Test
    public void testRegister() throws Exception {
        //1
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        dongRegistry.register(serviceMetaInfo);
        //2
        serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1235);
        dongRegistry.register(serviceMetaInfo);
        // 3
        serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("2.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        dongRegistry.register(serviceMetaInfo);
    }

    @Test
    public void testUnRegister() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        System.out.println(serviceMetaInfo.getServiceKey());
        System.out.println(serviceMetaInfo.getServiceNodeKey());
        dongRegistry.unRegister(serviceMetaInfo);
    }

    @Test
    public void testServiceDiscovery() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        String servicekey = serviceMetaInfo.getServiceKey();
        List<ServiceMetaInfo> serviceMetaInfolist = dongRegistry.serviceDiscovery(servicekey);
        Assert.assertNotNull(serviceMetaInfolist);

    }
}