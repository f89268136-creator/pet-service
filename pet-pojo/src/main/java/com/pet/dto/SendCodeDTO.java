package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送登录验证码参数。
 */
@Data
public class SendCodeDTO implements Serializable {

    private String email;
}