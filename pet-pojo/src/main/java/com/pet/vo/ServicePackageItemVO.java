package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 套餐内服务项视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageItemVO implements Serializable {

    //服务项目名称
    private String name;

    //份数
    private Integer copies;

    //服务项目图片
    private String image;

    //服务项目描述
    private String description;
}
