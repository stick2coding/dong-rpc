package com.dong.example.provider.service.impl;

import com.dong.example.common.model.User;
import com.dong.example.common.service.OrderService;

import java.util.HashMap;
import java.util.Map;

public class OrderServiceImpl implements OrderService {

    final static Map<Integer, User> userMap = new HashMap<Integer, User>();

    static{
        User user = new User();
        user.setId(1);
        user.setName("dongdong");
        userMap.put(user.getId(), user);
        User user2 = new User();
        user2.setId(2);
        user2.setName("dongdong2");
        userMap.put(user2.getId(), user2);
    }

    @Override
    public User getUserById(int userId) {
        System.out.println("服务提供者的接口被调用，用户ID：" + userId);
        return userMap.get(userId);
    }
}
