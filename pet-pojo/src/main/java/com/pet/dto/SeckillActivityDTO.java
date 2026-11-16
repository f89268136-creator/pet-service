package com.pet.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动创建参数。
 */
@Data
public class SeckillActivityDTO implements Serializable {

    private String name;

    private Long setmealId;

    private BigDecimal seckillPrice;

    private Integer stock;

    private Integer limitPerUser;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;
}