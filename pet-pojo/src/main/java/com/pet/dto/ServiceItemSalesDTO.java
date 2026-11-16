package com.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 服务项目销量统计参数。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceItemSalesDTO implements Serializable {
    //商品名称
    private String name;

    //销量
    private Integer number;
}
