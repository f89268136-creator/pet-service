package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交预约请求参数。
 */
@Data
public class AppointmentSubmitDTO implements Serializable {

    private Long storeId;

    private Long petId;

    private Long slotId;

    private Long serviceId;

    private String remark;
}
