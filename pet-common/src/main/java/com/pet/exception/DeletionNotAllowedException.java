package com.pet.exception;

/**
 * 删除受限异常：当数据处于上架、被引用等不允许删除的状态时抛出。
 */
public class DeletionNotAllowedException extends BaseException {

    public DeletionNotAllowedException(String msg) {
        super(msg);
    }

}
