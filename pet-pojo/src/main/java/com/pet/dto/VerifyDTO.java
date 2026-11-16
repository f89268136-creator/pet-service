package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 核销请求参数。
 */
@Data
public class VerifyDTO implements Serializable {

    private String verifyCode;
}
