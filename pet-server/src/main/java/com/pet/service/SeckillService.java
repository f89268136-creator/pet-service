package com.pet.service;

import com.pet.dto.SeckillActivityDTO;
import com.pet.result.PageResult;
import com.pet.vo.SeckillActivityVO;
import com.pet.vo.SeckillGrabVO;
import com.pet.vo.SeckillOrderVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务套餐秒杀业务接口。
 *
 * <p>用户侧负责活动浏览、抢购、订单查询与支付；管理侧负责活动维护、订单核销；
 * 另有供 Stream 消费者调用的落库与库存回滚方法。</p>
 */
public interface SeckillService {

    /**
     * 查询进行中的秒杀活动。
     */
    List<SeckillActivityVO> listActive();

    /**
     * 查询秒杀活动详情。
     */
    SeckillActivityVO detail(Long id);

    /**
     * 抢购秒杀套餐，返回排队中的订单号。
     */
    SeckillGrabVO grab(Long activityId, Long userId);

    /**
     * 按订单号查询秒杀订单。
     */
    SeckillOrderVO getOrder(String orderNo, Long userId);

    /**
     * 分页查询当前用户的秒杀订单。
     */
    PageResult pageMyOrders(int page, int pageSize, Integer status, Long userId);

    /**
     * 模拟支付秒杀订单。
     */
    void pay(String orderNo, Long userId);

    /**
     * 管理端分页查询秒杀活动。
     */
    PageResult pageActivities(int page, int pageSize, Integer status, String keyword);

    /**
     * 新建秒杀活动。
     */
    SeckillActivityVO create(SeckillActivityDTO dto);

    /**
     * 启用或停用秒杀活动。
     */
    void updateStatus(Long id, Integer status);

    /**
     * 管理端分页查询秒杀订单。
     */
    PageResult pageAllOrders(int page, int pageSize, Integer status, String keyword);

    /**
     * 根据核销码核销秒杀订单。
     */
    void verify(String verifyCode);

    /**
     * 将 Stream 中的秒杀消息异步落库。
     */
    void persistFromStream(String orderNo, Long activityId, Long userId, BigDecimal amount, LocalDateTime createTime);

    /**
     * 回滚 Redis 中的秒杀预扣库存与用户标记。
     */
    void restoreReservation(Long activityId, Long userId);
}