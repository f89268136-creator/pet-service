package com.pet.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pet.config.RedissonManager;
import com.pet.constant.MessageConstant;
import com.pet.context.BaseContext;
import com.pet.dto.AppointmentSubmitDTO;
import com.pet.dto.AppointmentPageQueryDTO;
import com.pet.entity.AppointmentSlot;
import com.pet.entity.ServiceItem;
import com.pet.entity.AppointmentOrderDetail;
import com.pet.entity.AppointmentOrder;
import com.pet.entity.Pet;
import com.pet.entity.Store;
import com.pet.entity.User;
import com.pet.exception.BusinessException;
import com.pet.mapper.AppointmentSlotMapper;
import com.pet.mapper.ServiceItemMapper;
import com.pet.mapper.AppointmentOrderDetailMapper;
import com.pet.mapper.AppointmentOrderMapper;
import com.pet.mapper.PetMapper;
import com.pet.mapper.StoreMapper;
import com.pet.mapper.UserMapper;
import com.pet.result.PageResult;
import com.pet.service.AppointmentService;
import com.pet.vo.AppointmentVO;
import com.pet.vo.AppointmentSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 预约核心业务实现。
 *
 * <p>负责预约单从创建、支付、确认、到店核销、服务完成到取消/超时取消的完整状态流转，
 * 并通过 Redisson 分布式锁、条件更新和事务保证同一宠物的并发预约安全。</p>
 */
@Service
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private AppointmentOrderMapper orderMapper;
    @Autowired
    private AppointmentOrderDetailMapper orderDetailMapper;
    @Autowired
    private AppointmentSlotMapper slotMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private ServiceItemMapper dishMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedissonManager redissonManager;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 提交预约：按宠物维度加分布式锁，防止同一宠物并发重复下单，
     * 随后在事务中完成校验、库存扣减和订单落库。
     */
    @Override
    public AppointmentSubmitVO submit(AppointmentSubmitDTO submitDTO) {
        if (submitDTO.getStoreId() == null || submitDTO.getPetId() == null
                || submitDTO.getSlotId() == null || submitDTO.getServiceId() == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_PARAM_REQUIRED);
        }
        RLock lock = tryLockPet(submitDTO.getPetId());
        try {
            return transactionTemplate.execute(status -> doSubmit(submitDTO));
        } finally {
            unlock(lock);
        }
    }

    /**
     * 预约落库主流程：校验宠物归属、门店营业、服务上架和时段有效性，
     * 拒绝同一宠物的重复预约，以条件更新扣减时段库存后写入预约单及明细。
     */
    private AppointmentSubmitVO doSubmit(AppointmentSubmitDTO submitDTO) {
        Long userId = BaseContext.getCurrentId();
        Pet pet = checkPet(submitDTO.getPetId(), userId);
        Store store = checkStore(submitDTO.getStoreId());
        ServiceItem service = checkService(submitDTO.getServiceId());
        AppointmentSlot slot = checkSlot(submitDTO, store, service);

        Integer active = orderMapper.countActiveByPetId(pet.getId());
        if (active != null && active > 0) {
            throw new BusinessException(MessageConstant.PET_HAS_ACTIVE_APPOINTMENT);
        }

        // 以条件更新扣减库存，避免并发超卖
        if (slotMapper.incrementBooked(slot.getId()) == 0) {
            throw new BusinessException(MessageConstant.SLOT_FULL);
        }

        User user = userMapper.getById(userId);
        AppointmentOrder orders = AppointmentOrder.builder()
                .number(generateNumber())
                .status(AppointmentOrder.PENDING_PAYMENT)
                .userId(userId)
                .storeId(store.getId())
                .petId(pet.getId())
                .slotId(slot.getId())
                .serviceDate(slot.getSlotDate())
                .serviceTime(formatServiceTime(slot))
                .verifyCode(generateVerifyCode())
                .orderTime(LocalDateTime.now())
                .payStatus(AppointmentOrder.UN_PAID)
                .amount(service.getPrice())
                .remark(submitDTO.getRemark())
                .phone(user == null ? null : user.getPhone())
                .build();
        orderMapper.insert(orders);

        AppointmentOrderDetail orderDetail = AppointmentOrderDetail.builder()
                .orderId(orders.getId())
                .name(service.getName())
                .serviceItemId(service.getId())
                .number(1)
                .amount(service.getPrice())
                .image(service.getImage())
                .build();
        orderDetailMapper.insertBatch(Collections.singletonList(orderDetail));

        return AppointmentSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 模拟支付：仅待支付订单可支付，成功后进入待确认状态。
     */
    @Override
    public void pay(Long id) {
        AppointmentOrder ordersDB = orderMapper.getByIdAndUserId(id, BaseContext.getCurrentId());
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_NOT_FOUND);
        }
        if (!AppointmentOrder.PENDING_PAYMENT.equals(ordersDB.getStatus())) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
        // 演示环境使用模拟支付，正式接入微信支付时替换此处
        if (orderMapper.paySuccess(id, LocalDateTime.now()) == 0) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
    }

    /**
     * 分页查询当前登录用户的预约单。
     */
    @Override
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {
        PageHelper.startPage(page <= 0 ? 1 : page, pageSize <= 0 ? 10 : pageSize);
        AppointmentPageQueryDTO queryDTO = new AppointmentPageQueryDTO();
        queryDTO.setUserId(BaseContext.getCurrentId());
        queryDTO.setStatus(status);
        Page<AppointmentOrder> pageResult = orderMapper.pageQuery(queryDTO);
        return new PageResult(pageResult.getTotal(), toVOList(pageResult.getResult()));
    }

    /**
     * 查询当前用户自己的预约详情，非本人订单按不存在处理。
     */
    @Override
    public AppointmentVO detail(Long id) {
        AppointmentOrder ordersDB = orderMapper.getByIdAndUserId(id, BaseContext.getCurrentId());
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_NOT_FOUND);
        }
        return toVO(ordersDB);
    }

    /**
     * 用户取消预约：仅未开始的预约可取消，已支付订单会标记退款。
     */
    @Override
    @Transactional
    public void cancel(Long id, String reason) {
        AppointmentOrder ordersDB = orderMapper.getByIdAndUserId(id, BaseContext.getCurrentId());
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_NOT_FOUND);
        }
        cancelInternal(ordersDB, reason == null || reason.trim().isEmpty() ? "用户取消" : reason);
    }

    /**
     * 管理端按状态、日期、门店等条件分页查询预约单。
     */
    @Override
    public PageResult conditionSearch(AppointmentPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage() <= 0 ? 1 : queryDTO.getPage(),
                queryDTO.getPageSize() <= 0 ? 10 : queryDTO.getPageSize());
        Page<AppointmentOrder> pageResult = orderMapper.pageQuery(queryDTO);
        return new PageResult(pageResult.getTotal(), toVOList(pageResult.getResult()));
    }

    /**
     * 管理端查看任意预约单详情。
     */
    @Override
    public AppointmentVO adminDetail(Long id) {
        AppointmentOrder ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_NOT_FOUND);
        }
        return toVO(ordersDB);
    }

    /**
     * 商家确认预约：状态由待确认流转为待服务。
     */
    @Override
    public void confirm(Long id) {
        AppointmentOrder ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_NOT_FOUND);
        }
        if (!AppointmentOrder.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
        if (orderMapper.confirm(id) == 0) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
    }

    /**
     * 到店核销：根据核销码定位已确认订单，并将其流转为服务中。
     */
    @Override
    public void verify(String verifyCode) {
        if (verifyCode == null || verifyCode.trim().isEmpty()) {
            throw new BusinessException(MessageConstant.VERIFY_CODE_ERROR);
        }
        AppointmentOrder ordersDB = orderMapper.getByVerifyCode(verifyCode.trim());
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.VERIFY_CODE_ERROR);
        }
        if (!AppointmentOrder.CONFIRMED.equals(ordersDB.getStatus())) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
        if (orderMapper.startService(ordersDB.getId(), LocalDateTime.now()) == 0) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
    }

    /**
     * 完成服务：仅服务中的订单可完成。
     */
    @Override
    public void complete(Long id) {
        AppointmentOrder ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_NOT_FOUND);
        }
        if (!AppointmentOrder.IN_SERVICE.equals(ordersDB.getStatus())) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
        if (orderMapper.completeService(id, LocalDateTime.now()) == 0) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
    }

    /**
     * 商家取消预约，复用通用取消逻辑并按支付状态标记退款。
     */
    @Override
    @Transactional
    public void adminCancel(Long id, String reason) {
        AppointmentOrder ordersDB = orderMapper.getById(id);
        if (ordersDB == null) {
            throw new BusinessException(MessageConstant.APPOINTMENT_NOT_FOUND);
        }
        cancelInternal(ordersDB, reason == null || reason.trim().isEmpty() ? "商家取消" : reason);
    }

    /**
     * 超时未支付自动取消：仅处理仍处于待支付的订单，并归还时段已占用名额。
     */
    @Override
    @Transactional
    public void timeoutCancel(AppointmentOrder orders) {
        if (orders == null || orders.getId() == null) {
            return;
        }
        boolean refund = AppointmentOrder.PAID.equals(orders.getPayStatus());
        int rows = orderMapper.cancel(orders.getId(), AppointmentOrder.PENDING_PAYMENT,
                "预约超时，自动取消", LocalDateTime.now(), refund);
        if (rows > 0 && orders.getSlotId() != null) {
            slotMapper.decrementBooked(orders.getSlotId());
        }
    }

    /**
     * 取消状态机公共逻辑：仅待支付、待确认、待服务可取消，
     * 根据支付状态决定是否退款，成功后释放对应时段名额。
     */
    private void cancelInternal(AppointmentOrder ordersDB, String reason) {
        Integer status = ordersDB.getStatus();
        if (!AppointmentOrder.PENDING_PAYMENT.equals(status) && !AppointmentOrder.TO_BE_CONFIRMED.equals(status)
                && !AppointmentOrder.CONFIRMED.equals(status)) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
        boolean refund = AppointmentOrder.PAID.equals(ordersDB.getPayStatus());
        if (orderMapper.cancel(ordersDB.getId(), status, reason, LocalDateTime.now(), refund) == 0) {
            throw new BusinessException(MessageConstant.APPOINTMENT_STATUS_ERROR);
        }
        if (ordersDB.getSlotId() != null) {
            slotMapper.decrementBooked(ordersDB.getSlotId());
        }
    }

    /**
     * 校验宠物存在且属于当前用户，防止越权为他人宠物预约。
     */
    private Pet checkPet(Long petId, Long userId) {
        Pet pet = petMapper.getById(petId);
        if (pet == null) {
            throw new BusinessException(MessageConstant.PET_NOT_FOUND);
        }
        if (!pet.getUserId().equals(userId)) {
            throw new BusinessException(MessageConstant.PET_ACCESS_DENIED);
        }
        return pet;
    }

    /**
     * 校验门店存在且处于营业状态。
     */
    private Store checkStore(Long storeId) {
        Store store = storeMapper.getById(storeId);
        if (store == null) {
            throw new BusinessException(MessageConstant.STORE_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(store.getStatus())) {
            throw new BusinessException(MessageConstant.STORE_CLOSED);
        }
        return store;
    }

    /**
     * 校验服务项目存在且已上架。
     */
    private ServiceItem checkService(Long serviceId) {
        ServiceItem dish = dishMapper.getById(serviceId);
        if (dish == null) {
            throw new BusinessException(MessageConstant.SERVICE_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(dish.getStatus())) {
            throw new BusinessException(MessageConstant.SERVICE_OFF_SALE);
        }
        return dish;
    }

    /**
     * 校验时段属于指定门店与服务、处于开放状态且开始时间尚未过期。
     */
    private AppointmentSlot checkSlot(AppointmentSubmitDTO submitDTO, Store store, ServiceItem service) {
        AppointmentSlot slot = slotMapper.getById(submitDTO.getSlotId());
        if (slot == null || !slot.getStoreId().equals(store.getId())
                || !slot.getServiceId().equals(service.getId())) {
            throw new BusinessException(MessageConstant.SLOT_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(slot.getStatus())) {
            throw new BusinessException(MessageConstant.SLOT_CLOSED);
        }
        if (!LocalDateTime.of(slot.getSlotDate(), slot.getStartTime()).isAfter(LocalDateTime.now())) {
            throw new BusinessException(MessageConstant.SLOT_EXPIRED);
        }
        return slot;
    }

    /**
     * 批量转换预约单为展示对象。
     */
    private List<AppointmentVO> toVOList(List<AppointmentOrder> ordersList) {
        List<AppointmentVO> result = new ArrayList<>();
        for (AppointmentOrder orders : ordersList) {
            result.add(toVO(orders));
        }
        return result;
    }

    /**
     * 组装预约单展示信息，补齐宠物、门店、用户及服务明细。
     */
    private AppointmentVO toVO(AppointmentOrder orders) {
        AppointmentVO vo = new AppointmentVO();
        BeanUtils.copyProperties(orders, vo);
        vo.setStatusText(statusText(orders.getStatus()));

        Pet pet = petMapper.getById(orders.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
            vo.setPetSpecies(pet.getSpecies());
            vo.setPetBreed(pet.getBreed());
        }
        Store store = storeMapper.getById(orders.getStoreId());
        if (store != null) {
            vo.setStoreName(store.getName());
            vo.setStoreAddress(store.getAddress());
        }
        User user = userMapper.getById(orders.getUserId());
        if (user != null) {
            vo.setUserName(user.getName());
        }
        List<AppointmentOrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
        if (orderDetails != null && !orderDetails.isEmpty()) {
            AppointmentOrderDetail detail = orderDetails.get(0);
            vo.setServiceId(detail.getServiceItemId());
            vo.setServiceName(detail.getName());
            vo.setServiceImage(detail.getImage());
        }
        return vo;
    }

    /**
     * 预约状态码转换为中文文案。
     */
    private String statusText(Integer status) {
        if (AppointmentOrder.PENDING_PAYMENT.equals(status)) {
            return "待支付";
        }
        if (AppointmentOrder.TO_BE_CONFIRMED.equals(status)) {
            return "待确认";
        }
        if (AppointmentOrder.CONFIRMED.equals(status)) {
            return "待服务";
        }
        if (AppointmentOrder.IN_SERVICE.equals(status)) {
            return "服务中";
        }
        if (AppointmentOrder.COMPLETED.equals(status)) {
            return "已完成";
        }
        if (AppointmentOrder.CANCELLED.equals(status)) {
            return "已取消";
        }
        return "未知状态";
    }

    /**
     * 生成预约单号：时间戳 + 4 位随机数。
     */
    private String generateNumber() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

    /**
     * 生成 6 位到店核销码。
     */
    private String generateVerifyCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }

    /**
     * 将时段格式化为 HH:mm-HH:mm 的服务时间展示串。
     */
    private String formatServiceTime(AppointmentSlot slot) {
        return slot.getStartTime().format(TIME_FORMATTER) + "-" + slot.getEndTime().format(TIME_FORMATTER);
    }

    /**
     * 按宠物维度获取 Redisson 锁；等待 5 秒、持有 10 秒，获取失败视为系统繁忙。
     * Redis 未启用时返回 null，退化为数据库条件更新兜底。
     */
    private RLock tryLockPet(Long petId) {
        RedissonClient client = redissonManager.getClient();
        if (client == null) {
            return null;
        }
        RLock lock = client.getLock("pet:lock:appointment:pet:" + petId);
        try {
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                return lock;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        throw new BusinessException(MessageConstant.APPOINTMENT_BUSY);
    }

    /**
     * 仅释放当前线程持有的锁，避免误删其他线程的锁。
     */
    private void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
