package com.pet.mq;

import com.pet.dto.SeckillOrderMessage;
import com.pet.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 秒杀订单消费者。
 *
 * <p>异步落库并扣减数据库库存；落库失败时回滚 Redis 预扣，
 * 消息不再重回队列，避免坏消息反复消费。</p>
 */
@Component
@Slf4j
public class SeckillOrderConsumer {

    @Autowired
    private SeckillService seckillService;

    @RabbitListener(queues = "${pet.seckill.rabbit.queue}")
    public void onMessage(SeckillOrderMessage message) {
        if (message == null || message.getOrderNo() == null
                || message.getActivityId() == null || message.getUserId() == null) {
            log.warn("秒杀消息字段不完整，忽略：{}", message);
            return;
        }
        try {
            seckillService.persistOrder(message.getOrderNo(), message.getActivityId(),
                    message.getUserId(), message.getAmount(), toTime(message.getCreateTime()));
        } catch (Exception e) {
            log.error("秒杀订单落库失败，回滚 Redis 预扣，orderNo={}", message.getOrderNo(), e);
            seckillService.restoreReservation(message.getActivityId(), message.getUserId());
            throw new AmqpRejectAndDontRequeueException("秒杀订单落库失败", e);
        }
    }

    private LocalDateTime toTime(Long millis) {
        if (millis == null) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }
}