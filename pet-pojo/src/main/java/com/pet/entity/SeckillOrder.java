package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String orderNo;

    private Long activityId;

    private Long userId;

    private BigDecimal amount;

    /**
     * 0待支付 1待使用 2已使用 3已取消
     */
    private Integer status;

    private String verifyCode;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime verifyTime;
}