package com.pet.task;

import com.pet.entity.AppointmentOrder;
import com.pet.mapper.AppointmentOrderMapper;
import com.pet.service.AppointmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预约超时处理定时任务。
 *
 * <p>每分钟扫描一次超过 15 分钟仍未支付的预约单，并将其自动取消、释放时段库存。</p>
 */
@Component
@Slf4j
public class AppointmentTimeoutTask {

    @Autowired
    private AppointmentOrderMapper orderMapper;
    @Autowired
    private AppointmentService appointmentService;

    /**
     * 处理超时订单的方法
     */
    /**
     * 查找超时未支付的预约单并逐单取消。
     */
    @Scheduled(cron = "0 * * * * ? ") //每分钟触发一次
    public void processTimeoutOrder(){
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        // select * from orders where status = ? and order_time < (当前时间 - 15分钟)
        List<AppointmentOrder> ordersList = orderMapper.getByStatusAndOrderTimeLT(AppointmentOrder.PENDING_PAYMENT, time);

        if(ordersList != null && ordersList.size() > 0){
            for (AppointmentOrder orders : ordersList) {
                // 取消预约单并释放时段库存
                appointmentService.timeoutCancel(orders);
            }
        }
    }

}
