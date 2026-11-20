package com.pet.service;

import com.pet.constant.MessageConstant;
import com.pet.exception.LoginFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录风控限流器。
 *
 * <p>使用 Redis ZSET 记录时间窗口内的发送与失败次数，实现两级封禁：
 * 首次触发封禁 10 分钟，封禁期内再次触发升级为 30 分钟。</p>
 */
@Component
@Slf4j
public class LoginRateLimiter {

    private static final String BAN_KEY_PREFIX = "pet:login:ban:";
    private static final String BAN_LEVEL_KEY_PREFIX = "pet:login:banlevel:";
    private static final String SEND_KEY_PREFIX = "pet:login:send:";
    private static final String FAIL_KEY_PREFIX = "pet:login:fail:";

    /** 统计窗口：5 分钟 */
    private static final long WINDOW_MILLIS = 5 * 60 * 1000L;
    /** 两次发送的最小间隔 */
    private static final long SEND_INTERVAL_MILLIS = 60 * 1000L;
    /** 窗口内允许的发送次数 */
    private static final int SEND_WINDOW_LIMIT = 5;
    /** 窗口内允许的登录失败次数 */
    private static final int FAIL_WINDOW_LIMIT = 3;

    private static final long FIRST_BAN_SECONDS = 10 * 60L;
    private static final long SECOND_BAN_SECONDS = 30 * 60L;
    /** 封禁升级标记保留 24 小时，避免短期反复触发时始终只封 10 分钟 */
    private static final long BAN_LEVEL_TTL_SECONDS = 24 * 60 * 60L;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 已被封禁时直接拒绝，并提示剩余分钟数。
     */
    public void assertNotBanned(String target) {
        Long remain = stringRedisTemplate.getExpire(BAN_KEY_PREFIX + target, TimeUnit.SECONDS);
        if (remain != null && remain > 0) {
            throw new LoginFailedException(
                    String.format(MessageConstant.LOGIN_BANNED, Math.max(1, (remain + 59) / 60)));
        }
    }

    /**
     * 校验是否可以发送验证码：1 分钟内只允许一次，5 分钟内达到上限则封禁。
     */
    public void assertSendAllowed(String target) {
        assertNotBanned(target);
        long now = System.currentTimeMillis();
        String key = SEND_KEY_PREFIX + target;
        trim(key, now);
        Long recent = stringRedisTemplate.opsForZSet()
                .count(key, (double) (now - SEND_INTERVAL_MILLIS), (double) now);
        if (recent != null && recent > 0) {
            throw new LoginFailedException(MessageConstant.CODE_SEND_TOO_FREQUENT);
        }
        Long total = stringRedisTemplate.opsForZSet().zCard(key);
        if (total != null && total >= SEND_WINDOW_LIMIT) {
            escalateBan(target);
        }
    }

    /**
     * 发送成功后记录本次发送时间，ZSET 仅保留 5 分钟窗口。
     */
    public void recordSend(String target) {
        long now = System.currentTimeMillis();
        String key = SEND_KEY_PREFIX + target;
        trim(key, now);
        stringRedisTemplate.opsForZSet().add(key, now + ":" + Math.random(), now);
        stringRedisTemplate.expire(key, Duration.ofMillis(WINDOW_MILLIS));
    }

    /**
     * 记录一次登录失败，5 分钟内失败达到 3 次即封禁。
     */
    public void recordFailure(String target) {
        long now = System.currentTimeMillis();
        String key = FAIL_KEY_PREFIX + target;
        trim(key, now);
        stringRedisTemplate.opsForZSet().add(key, now + ":" + Math.random(), now);
        stringRedisTemplate.expire(key, Duration.ofMillis(WINDOW_MILLIS));
        Long total = stringRedisTemplate.opsForZSet().zCard(key);
        if (total != null && total >= FAIL_WINDOW_LIMIT) {
            escalateBan(target);
        }
    }

    /**
     * 登录成功后清空失败计数。
     */
    public void clearFailures(String target) {
        stringRedisTemplate.delete(FAIL_KEY_PREFIX + target);
    }

    /**
     * 一级封禁 10 分钟；24 小时内再次触发则升级为二级封禁 30 分钟。
     */
    private void escalateBan(String target) {
        String levelKey = BAN_LEVEL_KEY_PREFIX + target;
        String levelValue = stringRedisTemplate.opsForValue().get(levelKey);
        boolean escalated = "1".equals(levelValue) || "2".equals(levelValue);
        long seconds = escalated ? SECOND_BAN_SECONDS : FIRST_BAN_SECONDS;
        stringRedisTemplate.opsForValue().set(levelKey, escalated ? "2" : "1",
                Duration.ofSeconds(BAN_LEVEL_TTL_SECONDS));
        stringRedisTemplate.opsForValue().set(BAN_KEY_PREFIX + target, escalated ? "2" : "1",
                Duration.ofSeconds(seconds));
        log.warn("登录风控封禁，target={}，时长={}秒", target, seconds);
        throw new LoginFailedException(
                String.format(MessageConstant.LOGIN_BANNED, seconds / 60));
    }

    private void trim(String key, long now) {
        stringRedisTemplate.opsForZSet().removeRangeByScore(key, 0, now - WINDOW_MILLIS);
    }
}