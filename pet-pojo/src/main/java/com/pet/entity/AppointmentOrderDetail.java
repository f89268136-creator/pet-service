package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预约单服务明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentOrderDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //名称
    private String name;

    //预约单id
    private Long orderId;

    //服务项目id
    private Long serviceItemId;

    //数量
    private Integer number;

    //金额
    private BigDecimal amount;

    //图片
    private String image;
}
