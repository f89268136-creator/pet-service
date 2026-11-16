package com.pet.mapper;

import com.pet.entity.AppointmentSlot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 预约时段数据访问接口。
 *
 * <p>负责时段查询、库存原子扣减/归还以及时段维护。</p>
 */
@Mapper
public interface AppointmentSlotMapper {

    @Select("select * from appointment_slot where id = #{id}")
    AppointmentSlot getById(Long id);

    @Select("select * from appointment_slot " +
            "where store_id = #{storeId} and service_id = #{serviceId} and slot_date = #{slotDate} " +
            "and status = 1 and booked < capacity order by start_time")
    List<AppointmentSlot> listAvailable(Long storeId, Long serviceId, LocalDate slotDate);

    @Select("select * from appointment_slot " +
            "where store_id = #{storeId} and service_id = #{serviceId} and slot_date = #{slotDate} " +
            "order by start_time")
    List<AppointmentSlot> listByDate(Long storeId, Long serviceId, LocalDate slotDate);

    @Select("select count(1) from appointment_slot " +
            "where store_id = #{storeId} and service_id = #{serviceId} and slot_date = #{slotDate} and start_time = #{startTime}")
    int countByUniqueKey(Long storeId, Long serviceId, LocalDate slotDate, java.time.LocalTime startTime);

    @Insert("insert into appointment_slot (store_id, service_id, slot_date, start_time, end_time, capacity, booked, status, create_time, update_time) " +
            "values (#{storeId}, #{serviceId}, #{slotDate}, #{startTime}, #{endTime}, #{capacity}, #{booked}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(AppointmentSlot slot);

    @Update("update appointment_slot set booked = booked + 1, update_time = now() " +
            "where id = #{id} and status = 1 and booked < capacity")
    int incrementBooked(Long id);

    @Update("update appointment_slot set booked = booked - 1, update_time = now() " +
            "where id = #{id} and booked > 0")
    int decrementBooked(Long id);

    @Update("update appointment_slot set status = #{status}, update_time = now() where id = #{id}")
    int updateStatus(Long id, Integer status);
}
