package com.pet.mapper;

import com.pet.entity.SeckillActivity;
import com.pet.vo.SeckillActivityVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动数据访问接口。
 *
 * <p>提供活动查询、库存原子扣减和启停维护。</p>
 */
@Mapper
public interface SeckillActivityMapper {

    @Insert("insert into seckill_activity(name, setmeal_id, seckill_price, stock, limit_per_user, begin_time, end_time, status, create_time, update_time) " +
            "values(#{name}, #{setmealId}, #{seckillPrice}, #{stock}, #{limitPerUser}, #{beginTime}, #{endTime}, 1, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeckillActivity activity);

    @Select("select * from seckill_activity where id = #{id}")
    SeckillActivity getById(Long id);

    @Select("select a.*, s.name as setmeal_name, s.price as original_price, s.image as setmeal_image " +
            "from seckill_activity a left join setmeal s on a.setmeal_id = s.id " +
            "where a.status = 1 and a.begin_time <= #{now} and a.end_time >= #{now} order by a.id")
    List<SeckillActivityVO> listActiveVO(@Param("now") LocalDateTime now);

    @Select("<script>select a.*, s.name as setmeal_name, s.price as original_price, s.image as setmeal_image " +
            "from seckill_activity a left join setmeal s on a.setmeal_id = s.id <where>" +
            "<if test='status != null'>and a.status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'>and a.name like concat('%', #{keyword}, '%')</if>" +
            "</where> order by a.id desc</script>")
    List<SeckillActivityVO> pageVO(@Param("status") Integer status, @Param("keyword") String keyword);

    @Select("select a.*, s.name as setmeal_name, s.price as original_price, s.image as setmeal_image " +
            "from seckill_activity a left join setmeal s on a.setmeal_id = s.id where a.id = #{id}")
    SeckillActivityVO getVOById(Long id);

    @Update("update seckill_activity set status = #{status}, update_time = #{updateTime} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") LocalDateTime updateTime);

    @Update("update seckill_activity set stock = stock - 1 where id = #{id} and stock > 0")
    int decrementStock(Long id);

    @Update("update seckill_activity set stock = stock + 1 where id = #{id}")
    int restoreStock(Long id);
}