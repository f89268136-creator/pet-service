package com.pet.mapper;

import com.pet.entity.AppointmentOrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 预约单明细数据访问接口。
 */
@Mapper
public interface AppointmentOrderDetailMapper {
    /**
     * 批量插入订单明细数据
     * @param orderDetailList
     */
    void insertBatch(List<AppointmentOrderDetail> orderDetailList);

    /**
     * 根据订单id查询订单明细
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<AppointmentOrderDetail> getByOrderId(Long orderId);
}
