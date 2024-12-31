package com.dong.example.springboot.provider.service;

import com.dong.dongrpc.spring.boot.start.annotation.RpcService;
import com.dong.example.common.model.User;
import com.dong.example.common.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RpcService
public class UserServiceImpl implements UserService {

    final static Map<Integer, User> userMap = new HashMap<Integer, User>();

    static{
        User user = new User();
        user.setId(1);
        user.setName("dong");
        userMap.put(user.getId(), user);
        User user2 = new User();
        user2.setId(2);
        user2.setName("dong2");
        userMap.put(user2.getId(), user2);
    }

    /**
     * 实现接口、打印用户名、返回用户
     * @param user
     * @return
     */
    @Override
    public User getUser(User user) {
        System.out.println("服务提供者的接口被调用，输出内容：" + user.getName());
        return user;
    }

    @Override
    public User getUserById(int userId) {
        System.out.println("服务提供者的接口被调用，用户ID：" + userId);
        return userMap.get(userId);
    }
}
