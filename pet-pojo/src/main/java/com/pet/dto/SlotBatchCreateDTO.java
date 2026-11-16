package com.pet.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 批量生成预约时段参数。
 */
@Data
public class SlotBatchCreateDTO implements Serializable {

    private Long storeId;

    private List<Long> serviceIds;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 从 startDate 起连续生成的天数
     */
    private Integer days;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    /**
     * 时段长度（分钟）
     */
    private Integer intervalMinutes;

    private Integer capacity;
}
