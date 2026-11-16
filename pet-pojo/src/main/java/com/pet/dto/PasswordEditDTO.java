package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码参数。
 */
@Data
public class PasswordEditDTO implements Serializable {

    //员工id
    private Long empId;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

}
