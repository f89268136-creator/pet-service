package com.pet.service;

import com.pet.vo.BusinessDataVO;
import com.pet.vo.ServiceItemOverViewVO;
import com.pet.vo.AppointmentOverViewVO;
import com.pet.vo.ServicePackageOverViewVO;
import java.time.LocalDateTime;

/**
 * 工作台业务接口。
 */
public interface WorkspaceService {

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单管理数据
     * @return
     */
    AppointmentOverViewVO getAppointmentOverView();

    /**
     * 查询服务项目总览
     * @return
     */
    ServiceItemOverViewVO getDishOverView();

    /**
     * 查询服务套餐总览
     * @return
     */
    ServicePackageOverViewVO getSetmealOverView();

}
