package com.pet.controller.user;

import com.pet.constant.JwtClaimsConstant;
import com.pet.dto.DevLoginDTO;
import com.pet.dto.UserLoginDTO;
import com.pet.entity.User;
import com.pet.properties.JwtProperties;
import com.pet.result.Result;
import com.pet.service.UserService;
import com.pet.utils.JwtUtil;
import com.pet.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端登录接口。
 */
@RestController
@RequestMapping("/user/user")
@Api(tags = "C端用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("微信用户登录：{}",userLoginDTO.getCode());

        //微信登录
        User user = userService.wxLogin(userLoginDTO);
        return Result.success(buildLoginVO(user));
    }

    /**
     * 演示环境手机号登录
     * @param devLoginDTO
     * @return
     */
    @PostMapping("/dev-login")
    @ApiOperation("演示登录")
    public Result<UserLoginVO> devLogin(@RequestBody DevLoginDTO devLoginDTO){
        log.info("演示用户登录：{}", devLoginDTO.getPhone());
        User user = userService.devLogin(devLoginDTO.getPhone());
        return Result.success(buildLoginVO(user));
    }

    private UserLoginVO buildLoginVO(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
    }
}
