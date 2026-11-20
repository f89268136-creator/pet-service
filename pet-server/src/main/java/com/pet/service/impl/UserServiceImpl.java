package com.pet.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pet.constant.MessageConstant;
import com.pet.dto.EmailLoginDTO;
import com.pet.dto.UserLoginDTO;
import com.pet.entity.User;
import com.pet.exception.LoginFailedException;
import com.pet.mapper.UserMapper;
import com.pet.properties.WeChatProperties;
import com.pet.service.LoginCodeService;
import com.pet.service.UserService;
import com.pet.utils.HttpClientUtil;
import com.pet.vo.SendCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户业务实现。
 *
 * <p>支持微信小程序登录（code 换 openid）以及邮箱验证码登录，
 * 首次登录会自动创建用户。</p>
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private LoginCodeService loginCodeService;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        //判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);

        //如果是新用户，自动完成注册
        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        //返回这个用户对象
        return user;
    }

    /**
     * 发送邮箱登录验证码，发送频率与封禁状态由风控组件控制。
     */
    @Override
    public SendCodeVO sendLoginCode(String email) {
        return loginCodeService.send(email);
    }

    /**
     * 邮箱验证码登录，校验通过后按邮箱查询或自动注册用户。
     */
    @Override
    public User loginByCode(EmailLoginDTO emailLoginDTO) {
        String email = loginCodeService.verify(emailLoginDTO.getEmail(), emailLoginDTO.getCode());
        User user = userMapper.getByEmail(email);
        if (user != null) {
            return user;
        }
        user = User.builder()
                .email(email)
                .name("宠物用户")
                .createTime(LocalDateTime.now())
                .build();
        userMapper.insert(user);
        return user;
    }

    /**
     * 调用微信接口服务，获取微信用户的openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        //调用微信接口服务，获得当前微信用户的openid
        Map<String, String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}