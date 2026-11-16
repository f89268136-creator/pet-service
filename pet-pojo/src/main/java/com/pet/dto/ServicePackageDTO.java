package com.pet.dto;

import com.pet.entity.ServicePackageItem;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务套餐新增/修改参数。
 */
@Data
public class ServicePackageDTO implements Serializable {

    private Long id;

    //分类id
    private Long categoryId;

    //服务套餐名称
    private String name;

    //服务套餐价格
    private BigDecimal price;

    //状态 0:停用 1:启用
    private Integer status;

    //描述信息
    private String description;

    //图片
    private String image;

    //服务套餐服务项目关系
    private List<ServicePackageItem> setmealDishes = new ArrayList<>();

}
