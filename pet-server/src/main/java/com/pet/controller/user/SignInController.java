package com.pet.controller.user;

import com.pet.context.BaseContext;
import com.pet.result.Result;
import com.pet.service.SignInService;
import com.pet.vo.SignInStatsVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端签到接口。
 *
 * <p>提供每日签到和本月签到统计，签到成功会发放积分。</p>
 */
@RestController
@RequestMapping("/user/signin")
@Api(tags = "用户端签到接口")
public class SignInController {

    @Autowired
    private SignInService signInService;

    /**
     * 执行每日签到并返回最新签到统计。
     */
    @PostMapping
    @ApiOperation("每日签到")
    public Result<SignInStatsVO> signIn() {
        return Result.success(signInService.signIn(BaseContext.getCurrentId()));
    }

    /**
     * 查询本月签到统计。
     */
    @GetMapping("/stats")
    @ApiOperation("本月签到统计")
    public Result<SignInStatsVO> stats() {
        return Result.success(signInService.stats(BaseContext.getCurrentId()));
    }
}