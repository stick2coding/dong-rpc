package com.dong.example.common.service;

import com.dong.dongrpc.annotation.DongRpcService;
import com.dong.example.common.model.User;

// todo 这里有问题，不能直接把路径写在这里，应该配置在提供者内部，实际上直接在服务提供者内部维护
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
