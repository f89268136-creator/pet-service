package com.pet.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 单个预约时段创建参数。
 */
@Data
public class SlotCreateDTO implements Serializable {

    private Long storeId;

    private Long serviceId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate slotDate;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    private Integer capacity;

    private Integer status;
}
