package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理端取消预约请求参数。
 */
@Data
public class AppointmentCancelDTO implements Serializable {

    private Long id;
    //订单取消原因
    private String cancelReason;

}
