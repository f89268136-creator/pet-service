package com.pet.exception;

/**
 * 服务套餐启用失败异常
 */
public class ServicePackageEnableFailedException extends BaseException {

    public ServicePackageEnableFailedException(){}

    public ServicePackageEnableFailedException(String msg){
        super(msg);
    }
}
