package com.pet.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 基于 Redis Lua 的令牌桶限流器。
 *
 * <p>桶容量与每秒补充速率由调用方传入，脚本内按时间差补充令牌并原子扣减，
 * 多实例部署下同样有效。</p>
 */
@Component
@Slf4j
public class TokenBucketLimiter {

    /**
     * 返回 1 表示获取到令牌，0 表示被限流。
     */
    private static final DefaultRedisScript<Long> TOKEN_BUCKET_SCRIPT = new DefaultRedisScript<>(
            "local key = KEYS[1]\n" +
                    "local capacity = tonumber(ARGV[1])\n" +
                    "local rate = tonumber(ARGV[2])\n" +
                    "local now = tonumber(ARGV[3])\n" +
                    "local ttl = tonumber(ARGV[4])\n" +
                    "local data = redis.call('hmget', key, 'tokens', 'ts')\n" +
                    "local tokens = tonumber(data[1])\n" +
                    "local ts = tonumber(data[2])\n" +
                    "if tokens == nil or ts == nil then\n" +
                    "  tokens = capacity\n" +
                    "  ts = now\n" +
                    "end\n" +
                    "local delta = math.max(0, now - ts) / 1000\n" +
                    "tokens = math.min(capacity, tokens + delta * rate)\n" +
                    "if tokens < 1 then\n" +
                    "  redis.call('hmset', key, 'tokens', tokens, 'ts', now)\n" +
                    "  redis.call('expire', key, ttl)\n" +
                    "  return 0\n" +
                    "end\n" +
                    "redis.call('hmset', key, 'tokens', tokens - 1, 'ts', now)\n" +
                    "redis.call('expire', key, ttl)\n" +
                    "return 1",
            Long.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取一个令牌。
     *
     * @param key              桶标识
     * @param capacity         桶容量
     * @param permitsPerSecond 每秒补充的令牌数
     * @return 是否放行
     */
    public boolean tryAcquire(String key, int capacity, int permitsPerSecond) {
        if (capacity <= 0 || permitsPerSecond <= 0) {
            return true;
        }
        try {
            Long result = stringRedisTemplate.execute(TOKEN_BUCKET_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(capacity),
                    String.valueOf(permitsPerSecond),
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(Math.max(60, capacity / permitsPerSecond + 60)));
            return result != null && result == 1L;
        } catch (Exception e) {
            log.error("令牌桶限流脚本执行失败，key={}", key, e);
            // Redis 异常时不阻断业务，交由数据库与分布式锁兜底
            return true;
        }
    }
}