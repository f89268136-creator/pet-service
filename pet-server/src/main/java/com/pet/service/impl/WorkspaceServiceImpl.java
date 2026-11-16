package com.pet.service.impl;

import com.pet.constant.StatusConstant;
import com.pet.entity.AppointmentOrder;
import com.pet.mapper.ServiceItemMapper;
import com.pet.mapper.AppointmentOrderMapper;
import com.pet.mapper.ServicePackageMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.WorkspaceService;
import com.pet.vo.BusinessDataVO;
import com.pet.vo.ServiceItemOverViewVO;
import com.pet.vo.AppointmentOverViewVO;
import com.pet.vo.ServicePackageOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作台业务实现。
 *
 * <p>统计预约单各状态数量、服务项目/套餐上下架数量，以及指定区间的营业概览数据。</p>
 */
@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private AppointmentOrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ServiceItemMapper dishMapper;
    @Autowired
    private ServicePackageMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /**
         * 营业额：当日已完成订单的总金额
         * 有效订单：当日已完成订单的数量
         * 订单完成率：有效订单数 / 总订单数
         * 平均客单价：营业额 / 有效订单数
         * 新增用户：当日新增用户的数量
         */

        Map map = new HashMap();
        map.put("begin",begin);
        map.put("end",end);

        //查询总订单数
        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", AppointmentOrder.COMPLETED);
        //营业额
        Double turnover = orderMapper.sumByMap(map);
        turnover = turnover == null? 0.0 : turnover;

        //有效订单数
        Integer validOrderCount = orderMapper.countByMap(map);

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            //订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            //平均客单价
            unitPrice = turnover / validOrderCount;
        }

        //新增用户数
        Integer newUsers = userMapper.countByMap(map);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return
     */
    public AppointmentOverViewVO getAppointmentOverView() {
        Map map = new HashMap();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("status", AppointmentOrder.TO_BE_CONFIRMED);

        //待确认
        Integer waitingConfirm = orderMapper.countByMap(map);

        //待服务
        map.put("status", AppointmentOrder.CONFIRMED);
        Integer waitingService = orderMapper.countByMap(map);

        //已完成
        map.put("status", AppointmentOrder.COMPLETED);
        Integer completed = orderMapper.countByMap(map);

        //已取消
        map.put("status", AppointmentOrder.CANCELLED);
        Integer cancelled = orderMapper.countByMap(map);

        //全部订单
        map.put("status", null);
        Integer total = orderMapper.countByMap(map);

        return AppointmentOverViewVO.builder()
                .waitingConfirm(waitingConfirm)
                .waitingService(waitingService)
                .completed(completed)
                .cancelled(cancelled)
                .total(total)
                .build();
    }

    /**
     * 查询服务项目总览
     *
     * @return
     */
    public ServiceItemOverViewVO getDishOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return ServiceItemOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询服务套餐总览
     *
     * @return
     */
    public ServicePackageOverViewVO getSetmealOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return ServicePackageOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
