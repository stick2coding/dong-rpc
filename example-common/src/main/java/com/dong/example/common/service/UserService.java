package com.dong.example.common.service;

import com.dong.dongrpc.annotation.DongRpcService;
import com.dong.example.common.model.User;

@DongRpcService(name = "userService", implPath = "com.dong.example.provider.service.impl.UserServiceImpl")
public interface UserService {

    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);

    User getUserById(int userId);


}
