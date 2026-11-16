package com.pet.controller.admin;

import com.pet.dto.SlotBatchCreateDTO;
import com.pet.dto.SlotCreateDTO;
import com.pet.entity.AppointmentSlot;
import com.pet.result.Result;
import com.pet.service.AppointmentSlotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 管理端预约时段接口。
 *
 * <p>用于维护门店可预约时段，支持单个新增、批量生成、按日查询和启停。</p>
 */
@RestController("adminAppointmentSlotController")
@RequestMapping("/admin/appointment/slot")
@Api(tags = "管理端预约时段接口")
@Slf4j
public class AppointmentSlotController {

    @Autowired
    private AppointmentSlotService appointmentSlotService;

    /**
     * 新增单个预约时段。
     */
    @PostMapping
    @ApiOperation("新增时段")
    public Result create(@RequestBody SlotCreateDTO createDTO) {
        appointmentSlotService.create(createDTO);
        return Result.success();
    }

    /**
     * 按日期范围和间隔批量生成时段。
     */
    @PostMapping("/batch")
    @ApiOperation("批量生成时段")
    public Result<Integer> batchCreate(@RequestBody SlotBatchCreateDTO batchCreateDTO) {
        log.info("批量生成时段：{}", batchCreateDTO);
        return Result.success(appointmentSlotService.batchCreate(batchCreateDTO));
    }

    /**
     * 查询指定门店、服务、日期的时段列表。
     */
    @GetMapping("/list")
    @ApiOperation("查询时段列表")
    public Result<List<AppointmentSlot>> list(@RequestParam Long storeId,
                                              @RequestParam Long serviceId,
                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(appointmentSlotService.listByDate(storeId, serviceId, date));
    }

    /**
     * 开启或关闭时段。
     */
    @PutMapping("/{id}/status")
    @ApiOperation("开启或关闭时段")
    public Result updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        appointmentSlotService.updateStatus(id, status);
        return Result.success();
    }
}
