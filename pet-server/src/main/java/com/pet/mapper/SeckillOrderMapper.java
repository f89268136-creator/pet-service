package com.pet.mapper;

import com.pet.entity.SeckillOrder;
import com.pet.vo.SeckillOrderVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀订单数据访问接口。
 *
 * <p>提供订单查询、支付、核销与幂等落库支持。</p>
 */
@Mapper
public interface SeckillOrderMapper {

    @Insert("insert into seckill_order(order_no, activity_id, user_id, amount, status, verify_code, create_time) " +
            "values(#{orderNo}, #{activityId}, #{userId}, #{amount}, 0, #{verifyCode}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillOrder order);

    @Select("select * from seckill_order where order_no = #{orderNo}")
    SeckillOrder getByOrderNo(String orderNo);

    @Select("select * from seckill_order where activity_id = #{activityId} and user_id = #{userId}")
    SeckillOrder getByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") Long userId);

    @Select("select * from seckill_order where verify_code = #{verifyCode}")
    SeckillOrder getByVerifyCode(String verifyCode);

    @Update("update seckill_order set status = 1, pay_time = #{payTime} where order_no = #{orderNo} and status = 0")
    int pay(@Param("orderNo") String orderNo, @Param("payTime") LocalDateTime payTime);

    @Update("update seckill_order set status = 2, verify_time = #{verifyTime} where verify_code = #{verifyCode} and status = 1")
    int verify(@Param("verifyCode") String verifyCode, @Param("verifyTime") LocalDateTime verifyTime);

    @Select("select o.*, a.name as activity_name, s.name as setmeal_name " +
            "from seckill_order o " +
            "left join seckill_activity a on o.activity_id = a.id " +
            "left join setmeal s on a.setmeal_id = s.id " +
            "where o.user_id = #{userId} order by o.create_time desc")
    List<SeckillOrderVO> listByUser(Long userId);

    @Select("select o.*, a.name as activity_name, s.name as setmeal_name " +
            "from seckill_order o " +
            "left join seckill_activity a on o.activity_id = a.id " +
            "left join setmeal s on a.setmeal_id = s.id " +
            "where o.order_no = #{orderNo}")
    SeckillOrderVO getVOByOrderNo(String orderNo);

    @Select("<script>select o.*, a.name as activity_name, s.name as setmeal_name " +
            "from seckill_order o " +
            "left join seckill_activity a on o.activity_id = a.id " +
            "left join setmeal s on a.setmeal_id = s.id " +
            "<where><if test='status != null'>and o.status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'>and (o.order_no like concat('%', #{keyword}, '%') " +
            "or a.name like concat('%', #{keyword}, '%'))</if></where> order by o.id desc</script>")
    List<SeckillOrderVO> pageQuery(@Param("status") Integer status, @Param("keyword") String keyword);
}