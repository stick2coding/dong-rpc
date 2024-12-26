package com.dong.example.common.service;

import com.dong.dongrpc.annotation.DongRpcService;
import com.dong.example.common.model.User;

@DongRpcService(name = "orderService", implPath = "com.dong.example.provider.service.impl.OrderServiceImpl")
public interface OrderService {

    User getUserById(int userId);

}
