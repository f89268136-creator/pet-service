package com.pet.service;

import com.pet.dto.AppointmentSubmitDTO;
import com.pet.dto.AppointmentPageQueryDTO;
import com.pet.entity.AppointmentOrder;
import com.pet.result.PageResult;
import com.pet.vo.AppointmentVO;
import com.pet.vo.AppointmentSubmitVO;

/**
 * 预约业务接口。
 *
 * <p>覆盖用户端下单、支付、查询、取消，以及管理端确认、核销、完成、取消和超时处理。</p>
 */
public interface AppointmentService {

    /**
     * 提交预约并返回预约单基本信息。
     */
    AppointmentSubmitVO submit(AppointmentSubmitDTO submitDTO);

    /**
     * 模拟支付预约单。
     */
    void pay(Long id);

    /**
     * 分页查询当前用户的预约单。
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /**
     * 查询当前用户的预约详情。
     */
    AppointmentVO detail(Long id);

    /**
     * 用户取消预约。
     */
    void cancel(Long id, String reason);

    /**
     * 管理端条件分页查询预约单。
     */
    PageResult conditionSearch(AppointmentPageQueryDTO queryDTO);

    /**
     * 管理端查询预约详情。
     */
    AppointmentVO adminDetail(Long id);

    /**
     * 商家确认预约。
     */
    void confirm(Long id);

    /**
     * 根据核销码完成到店核销。
     */
    void verify(String verifyCode);

    /**
     * 完成服务。
     */
    void complete(Long id);

    /**
     * 商家取消预约。
     */
    void adminCancel(Long id, String reason);

    /**
     * 取消超时未支付的预约单。
     */
    void timeoutCancel(AppointmentOrder orders);
}
