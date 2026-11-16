package com.pet.controller.admin;

import com.pet.dto.SeckillActivityDTO;
import com.pet.dto.VerifyDTO;
import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.SeckillService;
import com.pet.vo.SeckillActivityVO;
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

/**
 * 管理端秒杀管理接口。
 *
 * <p>用于秒杀活动的查询、创建、启停，以及秒杀订单查询与核销。</p>
 */
@RestController
@RequestMapping("/admin/seckill")
@Api(tags = "管理端秒杀管理接口")
@Slf4j
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 分页查询秒杀活动。
     */
    @GetMapping("/page")
    @ApiOperation("秒杀活动分页查询")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   Integer status, String keyword) {
        return Result.success(seckillService.pageActivities(page, pageSize, status, keyword));
    }

    /**
     * 新增秒杀活动并初始化库存缓存。
     */
    @PostMapping
    @ApiOperation("新增秒杀活动")
    public Result<SeckillActivityVO> create(@RequestBody SeckillActivityDTO dto) {
        return Result.success(seckillService.create(dto));
    }

    /**
     * 启用或停用秒杀活动。
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用或停用秒杀活动")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        seckillService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 分页查询全部秒杀订单。
     */
    @GetMapping("/order/page")
    @ApiOperation("秒杀订单分页查询")
    public Result<PageResult> orderPage(@RequestParam(defaultValue = "1") int page,
                                        @RequestParam(defaultValue = "10") int pageSize,
                                        Integer status, String keyword) {
        return Result.success(seckillService.pageAllOrders(page, pageSize, status, keyword));
    }

    /**
     * 根据核销码核销秒杀订单。
     */
    @PutMapping("/order/verify")
    @ApiOperation("核销秒杀订单")
    public Result verify(@RequestBody VerifyDTO verifyDTO) {
        seckillService.verify(verifyDTO.getVerifyCode());
        return Result.success();
    }
}