package com.pet.exception;

/**
 * 业务异常：用于表达可预期的业务规则校验失败，由全局异常处理器转换为提示信息。
 */
public class BusinessException extends BaseException {

    public BusinessException(String msg) {
        super(msg);
    }

}
