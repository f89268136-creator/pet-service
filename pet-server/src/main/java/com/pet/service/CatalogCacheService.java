package com.pet.service;

import com.alibaba.fastjson.JSON;
import com.pet.config.RedissonManager;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 目录类数据（服务项目/服务套餐/门店）缓存治理：
 * 随机 TTL 防雪崩、空值缓存防穿透、重建锁防击穿、Redis 故障回退数据库。
 */
@Service
@Slf4j
public class CatalogCacheService {

    private static final long TTL_SECONDS = 1800;
    private static final long TTL_JITTER_SECONDS = 300;
    private static final long EMPTY_TTL_SECONDS = 60;
    private static final String LOCK_PREFIX = "pet:cache:lock:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonManager redissonManager;

    /**
     * 读取列表缓存，未命中时在分布式锁保护下回源并写回；
     * 空列表使用短 TTL 防止缓存穿透，正常数据 TTL 带随机抖动防止雪崩。
     */
    public <T> List<T> getList(String key, Class<T> type, Supplier<List<T>> loader) {
        List<T> cached = readList(key, type);
        if (cached != null) {
            return cached;
        }

        RLock lock = tryLock(key);
        try {
            if (lock == null) {
                return loadWithoutLock(key, type, loader);
            }
            cached = readList(key, type);
            if (cached != null) {
                return cached;
            }
            List<T> list = loader.get();
            if (list == null) {
                list = Collections.emptyList();
            }
            long ttl = list.isEmpty()
                    ? EMPTY_TTL_SECONDS
                    : TTL_SECONDS + ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS);
            write(key, JSON.toJSONString(list), ttl);
            return list;
        } finally {
            unlock(lock);
        }
    }

    /**
     * 读取对象缓存，未命中时回源；对象为空时不缓存。
     */
    public <T> T getObject(String key, Class<T> type, Supplier<T> loader) {
        T cached = readObject(key, type);
        if (cached != null) {
            return cached;
        }
        T value = loader.get();
        if (value != null) {
            long ttl = TTL_SECONDS + ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS);
            write(key, JSON.toJSONString(value), ttl);
        }
        return value;
    }

    /**
     * 删除单个缓存 key，失败仅记录日志。
     */
    public void evict(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("清理缓存失败，key={}：{}", key, e.getMessage());
        }
    }

    /**
     * 按前缀批量删除缓存，用于目录数据变更后的整体失效。
     */
    public void evictByPrefix(String prefix) {
        try {
            java.util.Set<String> keys = stringRedisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("批量清理缓存失败，prefix={}：{}", prefix, e.getMessage());
        }
    }

    private <T> List<T> readList(String key, Class<T> type) {
        String json = read(key);
        if (json == null) {
            return null;
        }
        return JSON.parseArray(json, type);
    }

    private <T> T readObject(String key, Class<T> type) {
        String json = read(key);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json, type);
    }

    private String read(String key) {
        try {
            return stringRedisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("读取缓存失败，key={}：{}", key, e.getMessage());
            return null;
        }
    }

    private void write(String key, String value, long ttlSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入缓存失败，key={}：{}", key, e.getMessage());
        }
    }

    /**
     * 未拿到重建锁时的降级策略：短暂等待后重读缓存，仍为空则直接回源。
     */
    private <T> List<T> loadWithoutLock(String key, Class<T> type, Supplier<List<T>> loader) {
        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<T> cached = readList(key, type);
        if (cached != null) {
            return cached;
        }
        List<T> list = loader.get();
        return list == null ? Collections.emptyList() : list;
    }

    /**
     * 尝试获取缓存重建锁，等待 200ms；Redisson 不可用时返回 null 走降级逻辑。
     */
    private RLock tryLock(String key) {
        RedissonClient client = redissonManager.getClient();
        if (client == null) {
            return null;
        }
        try {
            RLock lock = client.getLock(LOCK_PREFIX + key);
            return lock.tryLock(200, 10000, TimeUnit.MILLISECONDS) ? lock : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            log.warn("获取缓存重建锁失败，key={}：{}", key, e.getMessage());
            return null;
        }
    }

    private void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}