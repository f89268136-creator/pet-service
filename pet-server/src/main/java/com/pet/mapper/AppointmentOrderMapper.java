package com.pet.mapper;

import com.github.pagehelper.Page;
import com.pet.dto.ServiceItemSalesDTO;
import com.pet.dto.AppointmentPageQueryDTO;
import com.pet.entity.AppointmentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预约单数据访问接口。
 */
@Mapper
public interface AppointmentOrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(AppointmentOrder orders);

    /**
     * 根据订单号和用户id查询订单
     * @param orderNumber
     * @param userId
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    AppointmentOrder getByNumberAndUserId(String orderNumber, Long userId);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(AppointmentOrder orders);

    /**
     * 分页条件查询并按下单时间排序
     * @param ordersPageQueryDTO
     */
    Page<AppointmentOrder> pageQuery(AppointmentPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     */
    @Select("select * from orders where id=#{id}")
    AppointmentOrder getById(Long id);

    /**
     * 根据状态统计订单数量
     * @param status
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根据订单状态和下单时间查询订单
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<AppointmentOrder> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 根据动态条件统计营业额数据
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 统计指定时间区间内的销量排名前10
     * @param begin
     * @param end
     * @return
     */
    List<ServiceItemSalesDTO> getSalesTop10(LocalDateTime begin,LocalDateTime end);

    /**
     * 根据id和用户id查询预约单
     */
    @Select("select * from orders where id = #{id} and user_id = #{userId}")
    AppointmentOrder getByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据核销码查询预约单
     */
    @Select("select * from orders where verify_code = #{verifyCode}")
    AppointmentOrder getByVerifyCode(@Param("verifyCode") String verifyCode);

    /**
     * 统计宠物进行中的预约数量（待支付/待确认/已确认/服务中）
     */
    @Select("select count(1) from orders where pet_id = #{petId} and status in (1,2,3,4)")
    Integer countActiveByPetId(@Param("petId") Long petId);

    /**
     * 支付成功：仅待支付状态可流转
     */
    int paySuccess(@Param("id") Long id, @Param("checkoutTime") LocalDateTime checkoutTime);

    /**
     * 商家确认：仅待确认状态可流转
     */
    int confirm(@Param("id") Long id);

    /**
     * 到店核销：仅已确认状态可流转
     */
    int startService(@Param("id") Long id, @Param("verifyTime") LocalDateTime verifyTime);

    /**
     * 完成服务：仅服务中状态可流转
     */
    int completeService(@Param("id") Long id, @Param("serviceEndTime") LocalDateTime serviceEndTime);

    /**
     * 取消预约：仅当前状态仍为 fromStatus 时可流转
     */
    int cancel(@Param("id") Long id,
               @Param("fromStatus") Integer fromStatus,
               @Param("cancelReason") String cancelReason,
               @Param("cancelTime") LocalDateTime cancelTime,
               @Param("refund") boolean refund);
}
