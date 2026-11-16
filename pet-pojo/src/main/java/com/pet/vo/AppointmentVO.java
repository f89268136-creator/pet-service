package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约单详情视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentVO implements Serializable {

    private Long id;

    private String number;

    private Integer status;

    private String statusText;

    private Long userId;

    private String userName;

    private String phone;

    private Long petId;

    private String petName;

    private String petSpecies;

    private String petBreed;

    private Long storeId;

    private String storeName;

    private String storeAddress;

    private Long slotId;

    private LocalDate serviceDate;

    private String serviceTime;

    private Long serviceId;

    private String serviceName;

    private String serviceImage;

    private BigDecimal amount;

    private Integer payStatus;

    private Integer payMethod;

    private String remark;

    private String verifyCode;

    private LocalDateTime verifyTime;

    private LocalDateTime orderTime;

    private LocalDateTime checkoutTime;

    private LocalDateTime serviceStartTime;

    private LocalDateTime serviceEndTime;

    private String cancelReason;
}
