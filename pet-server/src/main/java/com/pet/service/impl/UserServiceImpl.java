package com.pet.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pet.constant.MessageConstant;
import com.pet.dto.UserLoginDTO;
import com.pet.entity.User;
import com.pet.exception.LoginFailedException;
import com.pet.mapper.UserMapper;
import com.pet.properties.WeChatProperties;
import com.pet.service.UserService;
import com.pet.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户业务实现。
 *
 * <p>支持微信小程序登录（code 换 openid）以及演示环境手机号登录，
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

    @Value("${pet.demo-login-enabled:false}")
    private boolean demoLoginEnabled;

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
     * 演示环境手机号登录，避免依赖微信授权
     * @param phone
     * @return
     */
    public User devLogin(String phone) {
        if (!demoLoginEnabled || phone == null || phone.trim().isEmpty()) {
            throw new LoginFailedException(MessageConstant.DEV_LOGIN_DISABLED);
        }
        String normalizedPhone = phone.trim();
        User user = userMapper.getByPhone(normalizedPhone);
        if (user == null) {
            user = User.builder()
                    .openid("dev-" + normalizedPhone)
                    .name("演示用户")
                    .phone(normalizedPhone)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
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
