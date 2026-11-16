package com.pet.controller.user;

import com.pet.context.BaseContext;
import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.SeckillService;
import com.pet.vo.SeckillActivityVO;
import com.pet.vo.SeckillGrabVO;
import com.pet.vo.SeckillOrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端服务套餐秒杀接口。
 *
 * <p>提供活动浏览、抢购、秒杀订单查询与模拟支付。</p>
 */
@RestController("userSeckillController")
@RequestMapping("/user/seckill")
@Api(tags = "用户端服务套餐秒杀接口")
@Slf4j
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 查询当前进行中的秒杀活动。
     */
    @GetMapping("/list")
    @ApiOperation("进行中的秒杀活动")
    public Result<List<SeckillActivityVO>> list() {
        return Result.success(seckillService.listActive());
    }

    /**
     * 查询秒杀活动详情。
     */
    @GetMapping("/{id}")
    @ApiOperation("秒杀活动详情")
    public Result<SeckillActivityVO> detail(@PathVariable Long id) {
        return Result.success(seckillService.detail(id));
    }

    /**
     * 抢购秒杀套餐，成功返回排队中的订单号。
     */
    @PostMapping("/{id}/grab")
    @ApiOperation("抢购秒杀服务套餐")
    public Result<SeckillGrabVO> grab(@PathVariable Long id) {
        return Result.success(seckillService.grab(id, BaseContext.getCurrentId()));
    }

    /**
     * 分页查询我的秒杀订单。
     */
    @GetMapping("/order/page")
    @ApiOperation("我的秒杀订单")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   Integer status) {
        return Result.success(seckillService.pageMyOrders(page, pageSize, status, BaseContext.getCurrentId()));
    }

    /**
     * 按订单号查询秒杀订单。
     */
    @GetMapping("/order/{orderNo}")
    @ApiOperation("按订单号查询秒杀订单")
    public Result<SeckillOrderVO> order(@PathVariable String orderNo) {
        return Result.success(seckillService.getOrder(orderNo, BaseContext.getCurrentId()));
    }

    /**
     * 模拟支付秒杀订单。
     */
    @PostMapping("/order/{orderNo}/pay")
    @ApiOperation("模拟支付秒杀订单")
    public Result pay(@PathVariable String orderNo) {
        seckillService.pay(orderNo, BaseContext.getCurrentId());
        return Result.success();
    }
}