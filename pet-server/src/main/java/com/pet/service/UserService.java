package com.pet.service;

import com.pet.dto.UserLoginDTO;
import com.pet.entity.User;

/**
 * 用户业务接口。
 */
public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 演示环境手机号登录，开启配置后可用
     * @param phone
     * @return
     */
    User devLogin(String phone);
}
