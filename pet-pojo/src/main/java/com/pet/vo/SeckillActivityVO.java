package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivityVO implements Serializable {

    private Long id;

    private String name;

    private Long setmealId;

    private String setmealName;

    private String setmealImage;

    private BigDecimal originalPrice;

    private BigDecimal seckillPrice;

    private Integer stock;

    private Integer remain;

    private Integer limitPerUser;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    private Integer status;
}