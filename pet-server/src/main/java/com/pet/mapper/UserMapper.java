package com.pet.mapper;

import com.pet.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 插入数据
     * @param user
     */
    void insert(User user);

    @Select("select * from user where id = #{id}")
    User getById(Long userId);

    @Select("select * from user where phone = #{phone}")
    User getByPhone(String phone);

    @Select("select * from user where email = #{email}")
    User getByEmail(String email);

    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    @Update("update user set points = points + #{points} where id = #{userId}")
    int addPoints(@Param("userId") Long userId, @Param("points") Integer points);

    @Select("select points from user where id = #{userId}")
    Integer getPoints(Long userId);
}
