package com.pet.mapper;

import com.pet.entity.Pet;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 宠物档案数据访问接口。
 *
 * <p>增删改均带用户维度条件，避免越权操作他人宠物。</p>
 */
@Mapper
public interface PetMapper {

    @Select("select * from pet where user_id = #{userId} order by create_time desc")
    List<Pet> listByUserId(Long userId);

    @Select("select * from pet where id = #{id}")
    Pet getById(Long id);

    @Insert("insert into pet (user_id, name, species, breed, gender, birthday, weight, sterilized, vaccinated, avatar, note, create_time, update_time) " +
            "values (#{userId}, #{name}, #{species}, #{breed}, #{gender}, #{birthday}, #{weight}, #{sterilized}, #{vaccinated}, #{avatar}, #{note}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Pet pet);

    @Update("update pet set name = #{name}, species = #{species}, breed = #{breed}, gender = #{gender}, " +
            "birthday = #{birthday}, weight = #{weight}, sterilized = #{sterilized}, vaccinated = #{vaccinated}, " +
            "avatar = #{avatar}, note = #{note}, update_time = #{updateTime} " +
            "where id = #{id} and user_id = #{userId}")
    int update(Pet pet);

    @Delete("delete from pet where id = #{id} and user_id = #{userId}")
    int deleteByIdAndUserId(Long id, Long userId);
}
