package com.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 秒杀订单异步落库消息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNo;

    private Long activityId;

    private Long userId;

    private BigDecimal amount;

    /**
     * 下单时间（毫秒时间戳）
     */
    private Long createTime;
}