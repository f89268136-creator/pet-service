package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 服务分类新增/修改参数。
 */
@Data
public class CategoryDTO implements Serializable {

    //主键
    private Long id;

    //类型 1 服务项目分类 2 服务套餐分类
    private Integer type;

    //分类名称
    private String name;

    //排序
    private Integer sort;

}
