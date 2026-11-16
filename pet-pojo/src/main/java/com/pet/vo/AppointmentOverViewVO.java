package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 预约概览数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentOverViewVO implements Serializable {
    //待确认数量
    private Integer waitingConfirm;

    //待服务数量
    private Integer waitingService;

    //已完成数量
    private Integer completed;

    //已取消数量
    private Integer cancelled;

    //全部订单
    private Integer total;
}
