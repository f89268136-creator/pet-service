package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentOrder implements Serializable {

    /**
     * 预约状态 1待支付 2待确认 3已确认(待服务) 4服务中 5已完成 6已取消
     */
    public static final Integer PENDING_PAYMENT = 1;
    public static final Integer TO_BE_CONFIRMED = 2;
    public static final Integer CONFIRMED = 3;
    public static final Integer IN_SERVICE = 4;
    public static final Integer COMPLETED = 5;
    public static final Integer CANCELLED = 6;

    /**
     * 支付状态 0未支付 1已支付 2退款
     */
    public static final Integer UN_PAID = 0;
    public static final Integer PAID = 1;
    public static final Integer REFUND = 2;

    private static final long serialVersionUID = 1L;

    private Long id;

    //预约单号
    private String number;

    //预约状态 1待支付 2待确认 3已确认 4服务中 5已完成 6已取消
    private Integer status;

    //下单用户id
    private Long userId;

    //下单时间
    private LocalDateTime orderTime;

    //支付时间
    private LocalDateTime checkoutTime;

    //支付状态 0未支付 1已支付 2退款
    private Integer payStatus;

    //实收金额
    private BigDecimal amount;

    //备注
    private String remark;

    //手机号
    private String phone;

    //预约取消原因
    private String cancelReason;

    //预约取消时间
    private LocalDateTime cancelTime;

    //门店id
    private Long storeId;

    //宠物id
    private Long petId;

    //预约时段id
    private Long slotId;

    //预约日期
    private LocalDate serviceDate;

    //预约时间段
    private String serviceTime;

    //到店核销码
    private String verifyCode;

    //核销时间
    private LocalDateTime verifyTime;

    //开始服务时间
    private LocalDateTime serviceStartTime;

    //服务完成时间
    private LocalDateTime serviceEndTime;
}
