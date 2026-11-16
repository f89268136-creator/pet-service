package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 可预约时段（库存）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlot implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long storeId;


    private Long serviceId;

    private LocalDate slotDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;

    private Integer booked;

    /**
     * 0关闭 1开放
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
