package com.pet.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pet.constant.MessageConstant;
import com.pet.dto.SeckillActivityDTO;
import com.pet.entity.SeckillActivity;
import com.pet.entity.SeckillOrder;
import com.pet.entity.ServicePackage;
import com.pet.exception.BusinessException;
import com.pet.mapper.SeckillActivityMapper;
import com.pet.mapper.SeckillOrderMapper;
import com.pet.mapper.ServicePackageMapper;
import com.pet.result.PageResult;
import com.pet.service.SeckillService;
import com.pet.vo.SeckillActivityVO;
import com.pet.vo.SeckillGrabVO;
import com.pet.vo.SeckillOrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务套餐秒杀业务实现。
 *
 * <p>采用 Redis + Lua 原子预扣库存并记录已抢用户，抢购成功后通过 Redis Stream
 * 异步落库；数据库库存与订单由消费者最终写入，失败时回滚 Redis 预扣。</p>
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    public static final String STREAM_KEY = "pet:stream:seckill-order";
    private static final String STOCK_KEY_PREFIX = "pet:seckill:stock:";
    private static final String USERS_KEY_PREFIX = "pet:seckill:users:";
    private static final DateTimeFormatter ORDER_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 返回值：>=0 剩余库存，-1 售罄，-2 重复购买，-3 库存未初始化
     */
    private static final DefaultRedisScript<Long> GRAB_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then return -2 end\n" +
                    "local stock = redis.call('get', KEYS[1])\n" +
                    "if not stock then return -3 end\n" +
                    "if tonumber(stock) <= 0 then return -1 end\n" +
                    "redis.call('decr', KEYS[1])\n" +
                    "redis.call('sadd', KEYS[2], ARGV[1])\n" +
                    "return tonumber(stock) - 1",
            Long.class);

    @Autowired
    private SeckillActivityMapper activityMapper;
    @Autowired
    private SeckillOrderMapper orderMapper;
    @Autowired
    private ServicePackageMapper setmealMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询进行中的秒杀活动，并用 Redis 库存覆盖数据库库存展示。
     */
    @Override
    public List<SeckillActivityVO> listActive() {
        List<SeckillActivityVO> list = activityMapper.listActiveVO(LocalDateTime.now());
        for (SeckillActivityVO vo : list) {
            vo.setRemain(getRemain(vo.getId(), vo.getStock()));
        }
        return list;
    }

    /**
     * 查询秒杀活动详情，并补充实时剩余库存。
     */
    @Override
    public SeckillActivityVO detail(Long id) {
        SeckillActivityVO vo = activityMapper.getVOById(id);
        if (vo == null) {
            throw new BusinessException(MessageConstant.SECKILL_NOT_FOUND);
        }
        vo.setRemain(getRemain(id, vo.getStock()));
        return vo;
    }

    /**
     * 抢购秒杀套餐：校验活动状态与时间后，通过 Lua 脚本原子完成“查重 + 扣库存 + 记录用户”，
     * 成功则投递 Redis Stream 异步落库，投递失败时回滚预扣。
     */
    @Override
    public SeckillGrabVO grab(Long activityId, Long userId) {
        SeckillActivity activity = activityMapper.getById(activityId);
        if (activity == null) {
            throw new BusinessException(MessageConstant.SECKILL_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(activity.getStatus())) {
            throw new BusinessException(MessageConstant.SECKILL_OFF_SHELF);
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getBeginTime())) {
            throw new BusinessException(MessageConstant.SECKILL_NOT_STARTED);
        }
        if (now.isAfter(activity.getEndTime())) {
            throw new BusinessException(MessageConstant.SECKILL_ENDED);
        }
        if (orderMapper.getByActivityAndUser(activityId, userId) != null) {
            throw new BusinessException(MessageConstant.SECKILL_LIMIT_REACHED);
        }

        ensureStockCache(activity);

        Long result;
        try {
            result = stringRedisTemplate.execute(GRAB_SCRIPT,
                    Arrays.asList(stockKey(activityId), usersKey(activityId)),
                    String.valueOf(userId));
        } catch (Exception e) {
            log.error("秒杀脚本执行失败，activityId={}，userId={}", activityId, userId, e);
            throw new BusinessException(MessageConstant.SECKILL_UNAVAILABLE);
        }
        if (result == null) {
            throw new BusinessException(MessageConstant.SECKILL_UNAVAILABLE);
        }
        if (result == -1) {
            throw new BusinessException(MessageConstant.SECKILL_SOLD_OUT);
        }
        if (result == -2) {
            throw new BusinessException(MessageConstant.SECKILL_LIMIT_REACHED);
        }
        if (result == -3) {
            throw new BusinessException(MessageConstant.SECKILL_UNAVAILABLE);
        }

        String orderNo = "SK" + now.format(ORDER_TIME)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        try {
            Map<String, String> message = new HashMap<>();
            message.put("orderNo", orderNo);
            message.put("activityId", String.valueOf(activityId));
            message.put("userId", String.valueOf(userId));
            message.put("amount", activity.getSeckillPrice().toPlainString());
            message.put("createTime", now.toString());
            stringRedisTemplate.opsForStream().add(
                    StreamRecords.newRecord().in(STREAM_KEY).ofMap(message));
            stringRedisTemplate.opsForStream().trim(STREAM_KEY, 1000);
        } catch (Exception e) {
            log.error("秒杀消息投递失败，回滚 Redis 预扣库存，orderNo={}", orderNo, e);
            restoreReservation(activityId, userId);
            throw new BusinessException(MessageConstant.SECKILL_QUEUE_ERROR);
        }

        return SeckillGrabVO.builder()
                .orderNo(orderNo)
                .status(0)
                .statusText("排队中，请稍后刷新")
                .build();
    }

    /**
     * 查询秒杀订单，仅订单所属用户可见。
     */
    @Override
    public SeckillOrderVO getOrder(String orderNo, Long userId) {
        SeckillOrder order = orderMapper.getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.SECKILL_ORDER_NOT_FOUND);
        }
        SeckillOrderVO vo = orderMapper.getVOByOrderNo(orderNo);
        vo.setStatusText(statusText(order.getStatus()));
        return vo;
    }

    /**
     * 分页查询当前用户的秒杀订单。
     */
    @Override
    public PageResult pageMyOrders(int page, int pageSize, Integer status, Long userId) {
        PageHelper.startPage(page <= 0 ? 1 : page, pageSize <= 0 ? 10 : pageSize);
        Page<SeckillOrderVO> result = (Page<SeckillOrderVO>) orderMapper.listByUser(userId);
        result.forEach(vo -> vo.setStatusText(statusText(vo.getStatus())));
        return new PageResult(result.getTotal(), result.getResult());
    }

    /**
     * 模拟支付秒杀订单，仅订单所属用户可支付。
     */
    @Override
    public void pay(String orderNo, Long userId) {
        SeckillOrder order = orderMapper.getByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.SECKILL_ORDER_NOT_FOUND);
        }
        if (orderMapper.pay(orderNo, LocalDateTime.now()) == 0) {
            throw new BusinessException(MessageConstant.SECKILL_ORDER_STATUS_ERROR);
        }
    }

    /**
     * 管理端分页查询秒杀活动。
     */
    @Override
    public PageResult pageActivities(int page, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(page <= 0 ? 1 : page, pageSize <= 0 ? 10 : pageSize);
        Page<SeckillActivityVO> result = (Page<SeckillActivityVO>) activityMapper.pageVO(status, keyword);
        result.forEach(vo -> vo.setRemain(getRemain(vo.getId(), vo.getStock())));
        return new PageResult(result.getTotal(), result.getResult());
    }

    /**
     * 新建秒杀活动并预热 Redis 库存缓存。
     */
    @Override
    @Transactional
    public SeckillActivityVO create(SeckillActivityDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty() || dto.getSetmealId() == null
                || dto.getSeckillPrice() == null || dto.getStock() == null || dto.getStock() <= 0
                || dto.getBeginTime() == null || dto.getEndTime() == null
                || !dto.getEndTime().isAfter(dto.getBeginTime())) {
            throw new BusinessException(MessageConstant.SECKILL_PARAM_INVALID);
        }
        ServicePackage setmeal = setmealMapper.getById(dto.getSetmealId());
        if (setmeal == null) {
            throw new BusinessException(MessageConstant.PACKAGE_NOT_FOUND);
        }
        LocalDateTime now = LocalDateTime.now();
        SeckillActivity activity = SeckillActivity.builder()
                .name(dto.getName().trim())
                .setmealId(dto.getSetmealId())
                .seckillPrice(dto.getSeckillPrice())
                .stock(dto.getStock())
                .limitPerUser(dto.getLimitPerUser() == null ? 1 : dto.getLimitPerUser())
                .beginTime(dto.getBeginTime())
                .endTime(dto.getEndTime())
                .status(1)
                .createTime(now)
                .updateTime(now)
                .build();
        activityMapper.insert(activity);
        preloadStock(activity);
        return activityMapper.getVOById(activity.getId());
    }

    /**
     * 启用或停用秒杀活动；停用时清理库存缓存。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        if (activityMapper.updateStatus(id, status, LocalDateTime.now()) == 0) {
            throw new BusinessException(MessageConstant.SECKILL_NOT_FOUND);
        }
        if (Integer.valueOf(0).equals(status)) {
            deleteQuietly(stockKey(id));
        }
    }

    /**
     * 管理端分页查询全部秒杀订单。
     */
    @Override
    public PageResult pageAllOrders(int page, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(page <= 0 ? 1 : page, pageSize <= 0 ? 10 : pageSize);
        Page<SeckillOrderVO> result = (Page<SeckillOrderVO>) orderMapper.pageQuery(status, keyword);
        result.forEach(vo -> vo.setStatusText(statusText(vo.getStatus())));
        return new PageResult(result.getTotal(), result.getResult());
    }

    /**
     * 根据核销码核销秒杀订单。
     */
    @Override
    public void verify(String verifyCode) {
        if (verifyCode == null || verifyCode.trim().isEmpty()
                || orderMapper.verify(verifyCode.trim(), LocalDateTime.now()) == 0) {
            throw new BusinessException(MessageConstant.SECKILL_ORDER_STATUS_ERROR);
        }
    }

    /**
     * 消费 Stream 消息后落库：扣减数据库库存并写入秒杀订单，订单号已存在时直接幂等返回。
     */
    @Override
    @Transactional
    public void persistFromStream(String orderNo, Long activityId, Long userId,
                                  BigDecimal amount, LocalDateTime createTime) {
        if (orderMapper.getByOrderNo(orderNo) != null) {
            return;
        }
        if (activityMapper.decrementStock(activityId) == 0) {
            throw new IllegalStateException("数据库库存不足，activityId=" + activityId);
        }
        SeckillOrder order = SeckillOrder.builder()
                .orderNo(orderNo)
                .activityId(activityId)
                .userId(userId)
                .amount(amount)
                .status(0)
                .verifyCode(generateVerifyCode())
                .createTime(createTime == null ? LocalDateTime.now() : createTime)
                .build();
        orderMapper.insert(order);
        log.info("秒杀订单落库成功，orderNo={}", orderNo);
    }

    /**
     * 回滚 Redis 预扣：归还库存并移除用户抢购标记。
     */
    @Override
    public void restoreReservation(Long activityId, Long userId) {
        try {
            stringRedisTemplate.opsForValue().increment(stockKey(activityId));
            stringRedisTemplate.opsForSet().remove(usersKey(activityId), String.valueOf(userId));
        } catch (Exception e) {
            log.error("秒杀库存回滚失败，activityId={}，userId={}", activityId, userId, e);
        }
    }

    private void ensureStockCache(SeckillActivity activity) {
        try {
            Boolean exists = stringRedisTemplate.hasKey(stockKey(activity.getId()));
            if (!Boolean.TRUE.equals(exists)) {
                preloadStock(activity);
            }
        } catch (Exception e) {
            log.error("秒杀库存缓存初始化失败，activityId={}", activity.getId(), e);
            throw new BusinessException(MessageConstant.SECKILL_UNAVAILABLE);
        }
    }

    private void preloadStock(SeckillActivity activity) {
        long ttl = Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds() + 86400;
        if (ttl < 60) {
            ttl = 60;
        }
        stringRedisTemplate.opsForValue()
                .setIfAbsent(stockKey(activity.getId()), String.valueOf(activity.getStock()), Duration.ofSeconds(ttl));
    }

    private Integer getRemain(Long activityId, Integer fallback) {
        try {
            String value = stringRedisTemplate.opsForValue().get(stockKey(activityId));
            return value == null ? fallback : Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void deleteQuietly(String key) {
        try {
            stringRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("删除秒杀缓存失败，key={}：{}", key, e.getMessage());
        }
    }

    private String stockKey(Long activityId) {
        return STOCK_KEY_PREFIX + activityId;
    }

    private String usersKey(Long activityId) {
        return USERS_KEY_PREFIX + activityId;
    }

    private String generateVerifyCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }

    private String statusText(Integer status) {
        if (Integer.valueOf(0).equals(status)) {
            return "待支付";
        }
        if (Integer.valueOf(1).equals(status)) {
            return "待使用";
        }
        if (Integer.valueOf(2).equals(status)) {
            return "已使用";
        }
        if (Integer.valueOf(3).equals(status)) {
            return "已取消";
        }
        return "未知状态";
    }
}