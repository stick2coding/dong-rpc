package com.dong.example.springboot.consumer.service;

import com.dong.dongrpc.spring.boot.start.annotation.RpcReference;
import com.dong.example.common.service.OrderService;
import com.dong.example.common.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {

    //引入接口，标注是rpc接口，需要进行代理
    @RpcReference
    private UserService userService;

    @RpcReference
    private OrderService orderService;

    public void test() {
        System.out.println("消费者测试 test userservice: " + userService.getUserById(1).getName());
        System.out.println("消费者测试 orderservice: " + orderService.getUserById(1).getName());
    }

}
