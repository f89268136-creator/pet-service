package com.pet.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 可预约时段查询条件。
 */
@Data
public class AppointmentSlotQueryDTO implements Serializable {

    private Long storeId;

    private Long serviceId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
