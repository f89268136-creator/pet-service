package com.pet.controller.user;

import com.pet.dto.AppointmentSlotQueryDTO;
import com.pet.dto.AppointmentSubmitDTO;
import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.AppointmentService;
import com.pet.service.AppointmentSlotService;
import com.pet.vo.AppointmentSlotVO;
import com.pet.vo.AppointmentVO;
import com.pet.vo.AppointmentSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端预约接口。
 *
 * <p>提供可预约时段查询、提交预约、模拟支付、预约列表/详情和取消预约。</p>
 */
@RestController("userAppointmentController")
@RequestMapping("/user/appointment")
@Api(tags = "用户端预约接口")
@Slf4j
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private AppointmentSlotService appointmentSlotService;

    /**
     * 查询指定门店、服务、日期下仍可预约的时段。
     */
    @GetMapping("/slots")
    @ApiOperation("查询可预约时段")
    public Result<List<AppointmentSlotVO>> slots(AppointmentSlotQueryDTO queryDTO) {
        return Result.success(appointmentSlotService.listAvailable(queryDTO));
    }

    /**
     * 提交预约，返回预约单号、金额和下单时间。
     */
    @PostMapping("/submit")
    @ApiOperation("提交预约")
    public Result<AppointmentSubmitVO> submit(@RequestBody AppointmentSubmitDTO submitDTO) {
        log.info("提交预约：{}", submitDTO);
        return Result.success(appointmentService.submit(submitDTO));
    }

    /**
     * 模拟支付预约单。
     */
    @PostMapping("/{id}/pay")
    @ApiOperation("模拟支付")
    public Result pay(@PathVariable Long id) {
        appointmentService.pay(id);
        return Result.success();
    }

    /**
     * 分页查询我的预约，可按状态筛选。
     */
    @GetMapping("/page")
    @ApiOperation("我的预约列表")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   Integer status) {
        return Result.success(appointmentService.pageQuery4User(page, pageSize, status));
    }

    /**
     * 查询我的预约详情。
     */
    @GetMapping("/{id}")
    @ApiOperation("预约详情")
    public Result<AppointmentVO> detail(@PathVariable Long id) {
        return Result.success(appointmentService.detail(id));
    }

    /**
     * 取消预约，可附带取消原因。
     */
    @PutMapping("/{id}/cancel")
    @ApiOperation("取消预约")
    public Result cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        appointmentService.cancel(id, reason);
        return Result.success();
    }
}
