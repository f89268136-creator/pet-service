package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 可预约时段视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentSlotVO implements Serializable {

    private Long id;

    private Long storeId;

    private Long serviceId;

    private LocalDate slotDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer capacity;

    private Integer booked;

    private Integer remaining;

    private Integer status;
}
