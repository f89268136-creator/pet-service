package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务套餐分页查询条件。
 */
@Data
public class ServicePackagePageQueryDTO implements Serializable {

    private int page;

    private int pageSize;

    private String name;

    //分类id
    private Integer categoryId;

    //状态 0表示禁用 1表示启用
    private Integer status;

}
