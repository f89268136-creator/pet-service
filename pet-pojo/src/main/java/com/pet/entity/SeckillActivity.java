package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long setmealId;

    private BigDecimal seckillPrice;

    private Integer stock;

    private Integer limitPerUser;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;

    /**
     * 0停用 1启用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}