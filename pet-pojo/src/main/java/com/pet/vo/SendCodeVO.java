package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证码发送结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeVO implements Serializable {

    /**
     * 验证码有效期（秒）
     */
    private Integer expireSeconds;

    /**
     * 开发环境未开启邮件发送时直接返回验证码，便于联调
     */
    private String devCode;
}