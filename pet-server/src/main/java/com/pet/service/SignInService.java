package com.pet.service;

import com.pet.vo.SignInStatsVO;

/**
 * 用户签到业务接口。
 *
 * <p>提供每日签到和本月签到统计，统计结果包含连续签到天数与积分。</p>
 */
public interface SignInService {

    /**
     * 执行每日签到，重复签到会被拒绝。
     */
    SignInStatsVO signIn(Long userId);

    /**
     * 查询当前用户的签到统计。
     */
    SignInStatsVO stats(Long userId);
}