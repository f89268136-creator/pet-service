package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 附近门店视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NearbyStoreVO implements Serializable {

    private Long id;

    private String name;

    private String address;

    private String phone;

    private Double longitude;

    private Double latitude;

    /**
     * 距离，单位米
     */
    private Double distance;
}