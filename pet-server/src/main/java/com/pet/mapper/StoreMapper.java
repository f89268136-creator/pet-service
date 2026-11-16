package com.pet.mapper;

import com.pet.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 门店数据访问接口。
 */
@Mapper
public interface StoreMapper {

    @Select("select * from store where status = 1 order by id")
    List<Store> listActive();

    @Select("select * from store order by id")
    List<Store> listAll();

    @Select("select * from store where id = #{id}")
    Store getById(Long id);

    @Update("update store set name = #{name}, address = #{address}, phone = #{phone}, " +
            "longitude = #{longitude}, latitude = #{latitude}, open_time = #{openTime}, " +
            "close_time = #{closeTime}, description = #{description}, status = #{status}, " +
            "update_time = #{updateTime} where id = #{id}")
    int update(Store store);
}