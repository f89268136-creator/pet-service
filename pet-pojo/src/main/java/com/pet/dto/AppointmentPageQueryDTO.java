package com.pet.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约单分页查询条件。
 */
@Data
public class AppointmentPageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String number;

    private  String phone;

    private Integer status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private Long userId;

    private Long storeId;

    private Long petId;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate serviceDate;

}
