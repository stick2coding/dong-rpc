package com.dong.example.common.service;

import com.dong.example.common.model.User;

public interface UserService {

    /**
     * 获取用户
     * @param user
     * @return
     */
    User getUser(User user);

    User getUserById(int userId);


}
