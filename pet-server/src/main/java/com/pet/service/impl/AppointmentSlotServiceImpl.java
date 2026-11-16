package com.pet.service.impl;

import com.pet.constant.MessageConstant;
import com.pet.dto.AppointmentSlotQueryDTO;
import com.pet.dto.SlotBatchCreateDTO;
import com.pet.dto.SlotCreateDTO;
import com.pet.entity.AppointmentSlot;
import com.pet.entity.ServiceItem;
import com.pet.entity.Store;
import com.pet.exception.BusinessException;
import com.pet.mapper.AppointmentSlotMapper;
import com.pet.mapper.ServiceItemMapper;
import com.pet.mapper.StoreMapper;
import com.pet.service.AppointmentSlotService;
import com.pet.vo.AppointmentSlotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约时段业务实现。
 *
 * <p>负责时段查询、单个/批量生成和启停，创建时会校验门店营业状态、
 * 服务上架状态、时间区间及唯一性。</p>
 */
@Service
public class AppointmentSlotServiceImpl implements AppointmentSlotService {

    @Autowired
    private AppointmentSlotMapper slotMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private ServiceItemMapper dishMapper;

    /**
     * 查询可用时段：只返回开始时间晚于当前时刻的开放时段，并计算剩余名额。
     */
    @Override
    public List<AppointmentSlotVO> listAvailable(AppointmentSlotQueryDTO queryDTO) {
        checkQuery(queryDTO);
        validateService(queryDTO.getServiceId());
        LocalDateTime now = LocalDateTime.now();
        List<AppointmentSlotVO> result = new ArrayList<>();
        for (AppointmentSlot slot : slotMapper.listAvailable(queryDTO.getStoreId(), queryDTO.getServiceId(), queryDTO.getDate())) {
            if (LocalDateTime.of(slot.getSlotDate(), slot.getStartTime()).isAfter(now)) {
                result.add(toVO(slot));
            }
        }
        return result;
    }

    /**
     * 查询某天全部时段，供管理端维护使用。
     */
    @Override
    public List<AppointmentSlot> listByDate(Long storeId, Long serviceId, LocalDate date) {
        if (storeId == null || serviceId == null || date == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_PARAM_REQUIRED);
        }
        return slotMapper.listByDate(storeId, serviceId, date);
    }

    /**
     * 新增时段：校验时间区间、容量、门店和服务，并防止同一门店/服务/日期/开始时间重复。
     */
    @Override
    public void create(SlotCreateDTO createDTO) {
        checkSlotTime(createDTO.getStartTime(), createDTO.getEndTime());
        if (createDTO.getCapacity() == null || createDTO.getCapacity() <= 0) {
            throw new BusinessException(MessageConstant.SLOT_CAPACITY_INVALID);
        }
        validateStore(createDTO.getStoreId());
        validateService(createDTO.getServiceId());
        if (slotMapper.countByUniqueKey(createDTO.getStoreId(), createDTO.getServiceId(),
                createDTO.getSlotDate(), createDTO.getStartTime()) > 0) {
            throw new BusinessException(MessageConstant.SLOT_DUPLICATED);
        }

        AppointmentSlot slot = AppointmentSlot.builder()
                .storeId(createDTO.getStoreId())
                .serviceId(createDTO.getServiceId())
                .slotDate(createDTO.getSlotDate())
                .startTime(createDTO.getStartTime())
                .endTime(createDTO.getEndTime())
                .capacity(createDTO.getCapacity())
                .booked(0)
                .status(createDTO.getStatus() == null ? 1 : createDTO.getStatus())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        slotMapper.insert(slot);
    }

    /**
     * 批量生成时段：按服务、日期范围和时间间隔切分时段，
     * 跳过过去日期和重复时段，返回实际新增数量。
     */
    @Override
    public int batchCreate(SlotBatchCreateDTO batchCreateDTO) {
        checkSlotTime(batchCreateDTO.getStartTime(), batchCreateDTO.getEndTime());
        if (batchCreateDTO.getServiceIds() == null || batchCreateDTO.getServiceIds().isEmpty()
                || batchCreateDTO.getStartDate() == null
                || batchCreateDTO.getIntervalMinutes() == null || batchCreateDTO.getIntervalMinutes() <= 0
                || batchCreateDTO.getDays() == null || batchCreateDTO.getDays() <= 0) {
            throw new BusinessException(MessageConstant.APPOINTMENT_PARAM_REQUIRED);
        }
        validateStore(batchCreateDTO.getStoreId());
        int capacity = batchCreateDTO.getCapacity() == null ? 1 : batchCreateDTO.getCapacity();
        if (capacity <= 0) {
            throw new BusinessException(MessageConstant.SLOT_CAPACITY_INVALID);
        }

        LocalDate today = LocalDate.now();
        int created = 0;
        for (Long serviceId : batchCreateDTO.getServiceIds()) {
            validateService(serviceId);
            for (int day = 0; day < batchCreateDTO.getDays(); day++) {
                LocalDate slotDate = batchCreateDTO.getStartDate().plusDays(day);
                if (slotDate.isBefore(today)) {
                    continue;
                }
                LocalTime start = batchCreateDTO.getStartTime();
                while (!start.plusMinutes(batchCreateDTO.getIntervalMinutes()).isAfter(batchCreateDTO.getEndTime())) {
                    LocalTime end = start.plusMinutes(batchCreateDTO.getIntervalMinutes());
                    if (slotMapper.countByUniqueKey(batchCreateDTO.getStoreId(), serviceId, slotDate, start) == 0) {
                        slotMapper.insert(AppointmentSlot.builder()
                                .storeId(batchCreateDTO.getStoreId())
                                .serviceId(serviceId)
                                .slotDate(slotDate)
                                .startTime(start)
                                .endTime(end)
                                .capacity(capacity)
                                .booked(0)
                                .status(1)
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build());
                        created++;
                    }
                    start = end;
                }
            }
        }
        return created;
    }

    /**
     * 开启或关闭时段，状态仅允许 0（关闭）或 1（开放）。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        AppointmentSlot slot = slotMapper.getById(id);
        if (slot == null) {
            throw new BusinessException(MessageConstant.SLOT_NOT_FOUND);
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(MessageConstant.UNKNOWN_ERROR);
        }
        slotMapper.updateStatus(id, status);
    }

    /**
     * 校验查询参数完整。
     */
    private void checkQuery(AppointmentSlotQueryDTO queryDTO) {
        if (queryDTO.getStoreId() == null || queryDTO.getServiceId() == null || queryDTO.getDate() == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_PARAM_REQUIRED);
        }
    }

    /**
     * 校验开始时间早于结束时间。
     */
    private void checkSlotTime(LocalTime startTime, LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new BusinessException(MessageConstant.SLOT_TIME_INVALID);
        }
    }

    /**
     * 校验门店存在且营业。
     */
    private void validateStore(Long storeId) {
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BusinessException(MessageConstant.STORE_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(store.getStatus())) {
            throw new BusinessException(MessageConstant.STORE_CLOSED);
        }
    }

    /**
     * 校验服务项目存在且已上架。
     */
    private void validateService(Long serviceId) {
        ServiceItem dish = dishMapper.getById(serviceId);
        if (dish == null) {
            throw new BusinessException(MessageConstant.SERVICE_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(dish.getStatus())) {
            throw new BusinessException(MessageConstant.SERVICE_OFF_SALE);
        }
    }

    /**
     * 转换为展示对象，剩余名额按容量减已预约数计算且不小于 0。
     */
    private AppointmentSlotVO toVO(AppointmentSlot slot) {
        return AppointmentSlotVO.builder()
                .id(slot.getId())
                .storeId(slot.getStoreId())
                .serviceId(slot.getServiceId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .capacity(slot.getCapacity())
                .booked(slot.getBooked())
                .remaining(Math.max(slot.getCapacity() - slot.getBooked(), 0))
                .status(slot.getStatus())
                .build();
    }
}
