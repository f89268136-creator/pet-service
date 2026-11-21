package com.pet.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String ACCOUNT_LOCKED = "账号被锁定";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String CATEGORY_BE_RELATED_BY_PACKAGE = "当前分类关联了服务套餐，不能删除";
    public static final String CATEGORY_BE_RELATED_BY_SERVICE_ITEM = "当前分类关联了服务项目，不能删除";
    public static final String LOGIN_FAILED = "登录失败";
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String PACKAGE_ENABLE_FAILED = "服务套餐内包含未上架的服务项目，无法上架";
    public static final String SERVICE_ITEM_ON_SALE = "上架中的服务项目不能删除";
    public static final String PACKAGE_ON_SALE = "上架中的服务套餐不能删除";
    public static final String SERVICE_ITEM_BE_RELATED_BY_PACKAGE = "当前服务项目关联了服务套餐，不能删除";

    public static final String PET_NOT_FOUND = "宠物不存在";
    public static final String PET_NAME_REQUIRED = "宠物名称不能为空";
    public static final String PET_ACCESS_DENIED = "无权操作该宠物";
    public static final String PET_HAS_ACTIVE_APPOINTMENT = "该宠物存在进行中的预约，暂不能删除";
    public static final String STORE_NOT_FOUND = "门店不存在";
    public static final String STORE_CLOSED = "门店暂停营业";
    public static final String SERVICE_NOT_FOUND = "服务项目不存在";
    public static final String SERVICE_OFF_SALE = "服务项目已下架";
    public static final String SLOT_NOT_FOUND = "预约时段不存在";
    public static final String SLOT_CLOSED = "预约时段已关闭";
    public static final String SLOT_FULL = "该时段已约满，请选择其他时段";
    public static final String SLOT_EXPIRED = "该时段已过期，请选择其他时段";
    public static final String SLOT_DUPLICATED = "该时段已存在";
    public static final String SLOT_CAPACITY_INVALID = "时段容量必须大于0";
    public static final String SLOT_TIME_INVALID = "时段起止时间不正确";
    public static final String APPOINTMENT_PARAM_REQUIRED = "预约参数不完整";
    public static final String APPOINTMENT_NOT_FOUND = "预约单不存在";
    public static final String APPOINTMENT_STATUS_ERROR = "当前预约状态不支持该操作";
    public static final String VERIFY_CODE_ERROR = "核销码错误";

    public static final String LOGIN_REQUIRED = "请先登录";
    public static final String SECKILL_RATE_LIMITED = "当前抢购人数过多，请稍后再试";

    public static final String APPOINTMENT_BUSY = "当前宠物正在提交预约，请稍后重试";
    public static final String SECKILL_NOT_FOUND = "秒杀活动不存在";
    public static final String SECKILL_NOT_STARTED = "秒杀活动未开始";
    public static final String SECKILL_ENDED = "秒杀活动已结束";
    public static final String SECKILL_OFF_SHELF = "秒杀活动已下线";
    public static final String SECKILL_SOLD_OUT = "秒杀商品已售罄";
    public static final String SECKILL_LIMIT_REACHED = "每人限购一次";
    public static final String SECKILL_ORDER_NOT_FOUND = "秒杀订单不存在";
    public static final String SECKILL_ORDER_STATUS_ERROR = "当前秒杀订单状态不支持该操作";
    public static final String SECKILL_QUEUE_ERROR = "秒杀请求处理失败，请稍后重试";
    public static final String SECKILL_UNAVAILABLE = "秒杀服务暂不可用，请稍后重试";
    public static final String SECKILL_PARAM_INVALID = "秒杀活动参数不完整或时间设置错误";
    public static final String PACKAGE_NOT_FOUND = "服务套餐不存在";
    public static final String COMMUNITY_POST_NOT_FOUND = "动态不存在";
    public static final String COMMUNITY_CONTENT_REQUIRED = "动态内容不能为空";
    public static final String COMMUNITY_COMMENT_REQUIRED = "评论内容不能为空";
    public static final String SIGN_IN_DUPLICATED = "今天已经签到过了";
    public static final String STORE_NAME_REQUIRED = "门店名称不能为空";

}
