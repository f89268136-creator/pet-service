package com.pet.service;

import com.pet.constant.JwtClaimsConstant;
import com.pet.properties.JwtProperties;
import com.pet.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户端令牌管理。
 *
 * <p>JWT 同时写入 Redis：拦截器校验 Redis 中的令牌是否有效，
 * 剩余有效期低于阈值时签发新令牌并通过响应头下发，实现自动续期。</p>
 */
@Component
@Slf4j
public class UserTokenService {

    private static final String TOKEN_KEY_PREFIX = "pet:login:token:";

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 签发令牌并写入 Redis。
     */
    public String issue(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        stringRedisTemplate.opsForValue().set(tokenKey(userId), token,
                Duration.ofMillis(jwtProperties.getUserTtl()));
        return token;
    }

    /**
     * 判断令牌是否为 Redis 中当前有效的令牌。
     */
    public boolean isActive(Long userId, String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String cached = stringRedisTemplate.opsForValue().get(tokenKey(userId));
        return token.equals(cached);
    }

    /**
     * 剩余有效期不足阈值时续期，返回新令牌；无需续期返回 null。
     */
    public String refreshIfNeeded(Long userId, String token) {
        Long remain = stringRedisTemplate.getExpire(tokenKey(userId), TimeUnit.MILLISECONDS);
        if (remain == null || remain < 0 || remain > jwtProperties.getRefreshThresholdMs()) {
            return null;
        }
        return issue(userId);
    }

    /**
     * 退出登录时删除令牌。
     */
    public void remove(Long userId) {
        stringRedisTemplate.delete(tokenKey(userId));
    }

    private String tokenKey(Long userId) {
        return TOKEN_KEY_PREFIX + userId;
    }
}