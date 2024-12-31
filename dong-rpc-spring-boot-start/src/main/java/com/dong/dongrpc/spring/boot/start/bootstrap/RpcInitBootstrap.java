package com.dong.dongrpc.spring.boot.start.bootstrap;


import com.dong.dongrpc.RpcApplication;
import com.dong.dongrpc.config.RpcConfig;
import com.dong.dongrpc.server.tcp.TcpServer;
import com.dong.dongrpc.server.tcp.VertxTcpServer;
import com.dong.dongrpc.spring.boot.start.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 初始化框架
 * 要实现一个导入bean的接口
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取enable注解的属性
//        boolean needServer = (boolean) importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName())
//                .get("needServer");

        RpcApplication.init();

        // 全局配置
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        // 是否需要启动服务
        boolean needServer = rpcConfig.isNeedServer();
        if (needServer){
            TcpServer tcpServer = new VertxTcpServer();
            tcpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
        } else{
            log.info("不需要server");
        }
    }

}
