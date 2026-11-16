package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物档案
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String name;

    /**
     * DOG / CAT / OTHER
     */
    private String species;

    private String breed;

    /**
     * 0未知 1公 2母
     */
    private Integer gender;

    private LocalDate birthday;

    private BigDecimal weight;

    /**
     * 0否 1是
     */
    private Integer sterilized;

    /**
     * 0否 1是
     */
    private Integer vaccinated;

    private String avatar;

    private String note;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
