package com.pet.task;

import com.pet.service.SeckillService;
import com.pet.service.impl.SeckillServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 秒杀订单 Stream 消费者。
 *
 * <p>每秒拉取最多 10 条消息并异步落库，消费失败时回滚 Redis 预扣库存；
 * 通过消费组保证多实例下消息不被重复处理。</p>
 */
@Component
@Slf4j
public class SeckillOrderStreamConsumer {

    private static final String GROUP = "pet-seckill-consumer-group";
    private static final String CONSUMER = "consumer-1";
    private static final long WARN_INTERVAL_MILLIS = 30000;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SeckillService seckillService;

    private volatile boolean groupReady = false;
    private volatile long lastWarnAt = 0;

    /**
     * 定时拉取并处理 Stream 消息。
     */
    @Scheduled(fixedDelay = 1000)
    public void consume() {
        if (!ensureGroup()) {
            return;
        }
        try {
            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                    Consumer.from(GROUP, CONSUMER),
                    StreamReadOptions.empty().count(10).block(Duration.ofMillis(200)),
                    StreamOffset.create(SeckillServiceImpl.STREAM_KEY, ReadOffset.lastConsumed()));
            if (records == null) {
                return;
            }
            for (MapRecord<String, Object, Object> record : records) {
                handle(record);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("NOGROUP")) {
                groupReady = false;
            }
            warnThrottled("消费秒杀消息失败：{}", e.getMessage());
        }
    }

    /**
     * 确保消费组存在，重复创建时忽略 BUSYGROUP 错误。
     */
    private boolean ensureGroup() {
        if (groupReady) {
            return true;
        }
        try {
            stringRedisTemplate.opsForStream()
                    .createGroup(SeckillServiceImpl.STREAM_KEY, ReadOffset.from("0"), GROUP);
            groupReady = true;
            return true;
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                groupReady = true;
                return true;
            }
            warnThrottled("创建秒杀消费组失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析单条消息并落库，无论成功失败最终都会确认消息，避免重复消费。
     */
    private void handle(MapRecord<String, Object, Object> record) {
        Map<Object, Object> body = record.getValue();
        String orderNo = string(body.get("orderNo"));
        Long activityId = parseLong(body.get("activityId"));
        Long userId = parseLong(body.get("userId"));
        if (orderNo == null || activityId == null || userId == null) {
            log.warn("秒杀消息字段不完整，忽略：{}", body);
            acknowledge(record);
            return;
        }
        try {
            seckillService.persistFromStream(orderNo, activityId, userId,
                    parseAmount(body.get("amount")), parseTime(body.get("createTime")));
        } catch (Exception e) {
            log.error("秒杀订单落库失败，回滚 Redis 预扣，orderNo={}", orderNo, e);
            seckillService.restoreReservation(activityId, userId);
        }
        acknowledge(record);
    }

    /**
     * 确认消息已消费。
     */
    private void acknowledge(MapRecord<String, Object, Object> record) {
        try {
            stringRedisTemplate.opsForStream()
                    .acknowledge(SeckillServiceImpl.STREAM_KEY, GROUP, record.getId());
        } catch (Exception e) {
            warnThrottled("确认秒杀消息失败：{}", e.getMessage());
        }
    }

    /**
     * 限流告警，避免 Redis 异常时日志刷屏。
     */
    private void warnThrottled(String message, String detail) {
        long now = System.currentTimeMillis();
        if (now - lastWarnAt > WARN_INTERVAL_MILLIS) {
            lastWarnAt = now;
            log.warn(message, detail);
        }
    }

    private String string(Object value) {
        return value == null ? null : value.toString();
    }

    private Long parseLong(Object value) {
        try {
            return value == null ? null : Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseAmount(Object value) {
        try {
            return value == null ? BigDecimal.ZERO : new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private LocalDateTime parseTime(Object value) {
        try {
            return value == null ? LocalDateTime.now() : LocalDateTime.parse(value.toString());
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
