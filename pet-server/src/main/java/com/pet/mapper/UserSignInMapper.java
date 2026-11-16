package com.pet.mapper;

import com.pet.entity.UserSignIn;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户签到记录数据访问接口。
 */
@Mapper
public interface UserSignInMapper {

    @Insert("insert into user_sign_in(user_id, sign_date, points, create_time) " +
            "values(#{userId}, #{signDate}, #{points}, #{createTime})")
    int insert(UserSignIn signIn);

    @Select("select count(1) from user_sign_in where user_id = #{userId} and sign_date = #{signDate}")
    int exists(@Param("userId") Long userId, @Param("signDate") LocalDate signDate);

    @Select("select * from user_sign_in where user_id = #{userId} and sign_date between #{start} and #{end} order by sign_date")
    List<UserSignIn> listByRange(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Select("select coalesce(sum(points), 0) from user_sign_in where user_id = #{userId}")
    Integer sumPoints(Long userId);
}