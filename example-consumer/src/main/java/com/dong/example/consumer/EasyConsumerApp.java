package com.dong.example.consumer;

import com.dong.dongrpc.proxy.ServiceProxyFactory;
import com.dong.example.common.model.User;
import com.dong.example.common.service.UserService;
import com.dong.example.consumer.proxy.UserServiceProxy;

/**
 * Hello world!
 *
 */
public class EasyConsumerApp
{
    public static void main( String[] args ) {
        System.out.println("测试服务消费...");
        // 消费者调用服务提供者的接口
        // 这里需要获取到userservice的实现类对象
        //UserService userService = new UserServiceProxy();
//        User user = new User();
//        user.setName("dong");
        // 使用动态代理获取对象
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        // 调用接口
        User newUser = userService.getUserById(2);
        if (newUser == null){
            System.out.println("newUser is null...");
        }else {
            System.out.println(newUser.getName());
        }
        System.out.println("测试服务消费结束...");
    }
}
