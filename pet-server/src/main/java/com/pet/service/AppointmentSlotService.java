package com.pet.service;

import com.pet.dto.AppointmentSlotQueryDTO;
import com.pet.dto.SlotBatchCreateDTO;
import com.pet.dto.SlotCreateDTO;
import com.pet.entity.AppointmentSlot;
import com.pet.vo.AppointmentSlotVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约时段业务接口。
 *
 * <p>负责可用时段查询、单个/批量生成时段以及时段启停管理。</p>
 */
public interface AppointmentSlotService {

    /**
     * 查询指定门店、服务、日期下仍可预约的时段。
     */
    List<AppointmentSlotVO> listAvailable(AppointmentSlotQueryDTO queryDTO);

    /**
     * 查询某天的全部时段（管理端使用，不过滤可用状态）。
     */
    List<AppointmentSlot> listByDate(Long storeId, Long serviceId, LocalDate date);

    /**
     * 新增单个时段。
     */
    void create(SlotCreateDTO createDTO);

    /**
     * 按天、按服务批量生成等长时段，返回实际新增数量。
     */
    int batchCreate(SlotBatchCreateDTO batchCreateDTO);

    /**
     * 开启或关闭时段。
     */
    void updateStatus(Long id, Integer status);
}
