package com.pet.controller.user;

import com.pet.context.BaseContext;
import com.pet.dto.EmailLoginDTO;
import com.pet.dto.SendCodeDTO;
import com.pet.dto.UserLoginDTO;
import com.pet.entity.User;
import com.pet.result.Result;
import com.pet.service.UserService;
import com.pet.service.UserTokenService;
import com.pet.vo.SendCodeVO;
import com.pet.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private UserTokenService userTokenService;

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
     * 发送邮箱登录验证码
     * @param sendCodeDTO
     * @return
     */
    @PostMapping("/code")
    @ApiOperation("发送邮箱验证码")
    public Result<SendCodeVO> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        log.info("发送登录验证码：{}", sendCodeDTO.getEmail());
        return Result.success(userService.sendLoginCode(sendCodeDTO.getEmail()));
    }

    /**
     * 邮箱验证码登录
     * @param emailLoginDTO
     * @return
     */
    @PostMapping("/email-login")
    @ApiOperation("邮箱验证码登录")
    public Result<UserLoginVO> emailLogin(@RequestBody EmailLoginDTO emailLoginDTO) {
        log.info("邮箱验证码登录：{}", emailLoginDTO.getEmail());
        User user = userService.loginByCode(emailLoginDTO);
        return Result.success(buildLoginVO(user));
    }

    /**
     * 退出登录，令牌从 Redis 中删除后立即失效。
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result logout() {
        userTokenService.remove(BaseContext.getCurrentId());
        return Result.success();
    }

    private UserLoginVO buildLoginVO(User user) {
        return UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .phone(user.getPhone())
                .email(user.getEmail())
                .token(userTokenService.issue(user.getId()))
                .build();
    }
}