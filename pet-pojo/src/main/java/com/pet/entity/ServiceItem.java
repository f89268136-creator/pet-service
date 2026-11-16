package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItem implements Serializable {

    private static final long serialVersionUID = 1L;

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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

}
