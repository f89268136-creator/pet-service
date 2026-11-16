package com.pet.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;

/**
 * Redisson 懒加载：Redis 不可用时不影响应用启动，由调用方回退数据库。
 */
@Configuration
@Slf4j
public class RedissonManager {

    @Autowired
    private RedisProperties redisProperties;

    private volatile RedissonClient client;
    private volatile long lastFailureAt = 0;

    private static final long RETRY_INTERVAL_MILLIS = 30000;

    public RedissonClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    if (System.currentTimeMillis() - lastFailureAt < RETRY_INTERVAL_MILLIS) {
                        return null;
                    }
                    client = createClient();
                    if (client == null) {
                        lastFailureAt = System.currentTimeMillis();
                    }
                }
            }
        }
        return client;
    }

    private RedissonClient createClient() {
        try {
            String address = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
            Config config = new Config();
            SingleServerConfig serverConfig = config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectionMinimumIdleSize(2)
                    .setConnectionPoolSize(8)
                    .setConnectTimeout(2000)
                    .setTimeout(3000);
            if (StringUtils.hasText(redisProperties.getPassword())) {
                serverConfig.setPassword(redisProperties.getPassword());
            }
            RedissonClient created = Redisson.create(config);
            log.info("Redisson 客户端初始化成功：{}", address);
            return created;
        } catch (Exception e) {
            log.warn("Redisson 初始化失败，分布式锁将降级为本地执行：{}", e.getMessage());
            return null;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            client.shutdown();
        }
    }
}