package com.pet.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 宠物档案新增/修改参数。
 */
@Data
public class PetDTO implements Serializable {

    private Long id;

    private String name;

    private String species;

    private String breed;

    private Integer gender;

    private LocalDate birthday;

    private BigDecimal weight;

    private Integer sterilized;

    private Integer vaccinated;

    private String avatar;

    private String note;
}
