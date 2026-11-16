package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderVO implements Serializable {

    private Long id;

    private String orderNo;

    private Long activityId;

    private String activityName;

    private String setmealName;

    private BigDecimal amount;

    private Integer status;

    private String statusText;

    private String verifyCode;

    private LocalDateTime createTime;

    private LocalDateTime payTime;

    private LocalDateTime verifyTime;
}