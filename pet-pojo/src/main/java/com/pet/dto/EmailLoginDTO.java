package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 邮箱验证码登录参数。
 */
@Data
public class EmailLoginDTO implements Serializable {

    private String email;

    private String code;
}