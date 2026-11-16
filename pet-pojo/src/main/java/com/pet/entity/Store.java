package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 门店
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String address;

    private String phone;

    private Double longitude;

    private Double latitude;

    private LocalTime openTime;

    private LocalTime closeTime;

    private String images;

    private String description;

    /**
     * 0停业 1营业
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
