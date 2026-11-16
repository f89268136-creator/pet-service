package com.pet.controller.admin;

import com.pet.dto.AppointmentCancelDTO;
import com.pet.dto.AppointmentPageQueryDTO;
import com.pet.dto.VerifyDTO;
import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.AppointmentService;
import com.pet.vo.AppointmentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端预约管理接口。
 *
 * <p>提供预约单查询、确认、到店核销、完成服务与商家取消等管理操作。</p>
 */
@RestController("adminAppointmentController")
@RequestMapping("/admin/appointment")
@Api(tags = "管理端预约管理接口")
@Slf4j
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    /**
     * 按条件分页查询预约单。
     */
    @GetMapping("/page")
    @ApiOperation("预约单分页查询")
    public Result<PageResult> page(AppointmentPageQueryDTO queryDTO) {
        return Result.success(appointmentService.conditionSearch(queryDTO));
    }

    /**
     * 查询预约单详情。
     */
    @GetMapping("/{id}")
    @ApiOperation("预约单详情")
    public Result<AppointmentVO> detail(@PathVariable Long id) {
        return Result.success(appointmentService.adminDetail(id));
    }

    /**
     * 确认用户的预约。
     */
    @PutMapping("/{id}/confirm")
    @ApiOperation("确认预约")
    public Result confirm(@PathVariable Long id) {
        appointmentService.confirm(id);
        return Result.success();
    }

    /**
     * 根据核销码完成到店核销。
     */
    @PutMapping("/verify")
    @ApiOperation("核销到店")
    public Result verify(@RequestBody VerifyDTO verifyDTO) {
        log.info("核销预约：{}", verifyDTO.getVerifyCode());
        appointmentService.verify(verifyDTO.getVerifyCode());
        return Result.success();
    }

    /**
     * 将服务中的预约标记为已完成。
     */
    @PutMapping("/{id}/complete")
    @ApiOperation("完成服务")
    public Result complete(@PathVariable Long id) {
        appointmentService.complete(id);
        return Result.success();
    }

    /**
     * 商家取消预约。
     */
    @PutMapping("/cancel")
    @ApiOperation("商家取消预约")
    public Result cancel(@RequestBody AppointmentCancelDTO cancelDTO) {
        appointmentService.adminCancel(cancelDTO.getId(), cancelDTO.getCancelReason());
        return Result.success();
    }
}
