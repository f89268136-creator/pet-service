package com.pet.service;

import com.pet.dto.EmailLoginDTO;
import com.pet.dto.UserLoginDTO;
import com.pet.entity.User;
import com.pet.vo.SendCodeVO;

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
     * 发送邮箱登录验证码
     * @param email
     * @return
     */
    SendCodeVO sendLoginCode(String email);

    /**
     * 邮箱验证码登录，邮箱不存在时自动注册
     * @param emailLoginDTO
     * @return
     */
    User loginByCode(EmailLoginDTO emailLoginDTO);
}