package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀抢购结果视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeckillGrabVO implements Serializable {

    private String orderNo;

    private Integer status;

    private String statusText;
}