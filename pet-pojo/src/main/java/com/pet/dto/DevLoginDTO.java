package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 开发环境快捷登录参数。
 */
@Data
public class DevLoginDTO implements Serializable {

    private String phone;
}
