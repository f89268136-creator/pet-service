package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务套餐服务项目关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //服务套餐id
    private Long setmealId;

    //服务项目id
    private Long dishId;

    //服务项目名称 （冗余字段）
    private String name;

    //服务项目原价
    private BigDecimal price;

    //份数
    private Integer copies;
}
