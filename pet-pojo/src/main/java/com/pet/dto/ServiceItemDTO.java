package com.pet.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 服务项目新增/修改参数。
 */
@Data
public class ServiceItemDTO implements Serializable {

    private Long id;
    //服务项目名称
    private String name;
    //服务项目分类id
    private Long categoryId;
    //服务项目价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 停售 1 起售
    private Integer status;

}
