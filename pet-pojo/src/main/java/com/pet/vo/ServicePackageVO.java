package com.pet.vo;

import com.pet.entity.ServicePackageItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 服务套餐视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageVO implements Serializable {

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

    //更新时间
    private LocalDateTime updateTime;

    //分类名称
    private String categoryName;

    //服务套餐和服务项目的关联关系
    private List<ServicePackageItem> setmealDishes = new ArrayList<>();
}
