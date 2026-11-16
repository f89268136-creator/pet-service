package com.pet.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区点赞数据访问接口。
 */
@Mapper
public interface CommunityLikeMapper {

    @Select("select count(1) from community_like where post_id = #{postId} and user_id = #{userId}")
    int exists(@Param("postId") Long postId, @Param("userId") Long userId);

    @Insert("insert into community_like(post_id, user_id, create_time) values(#{postId}, #{userId}, #{createTime})")
    int insert(@Param("postId") Long postId, @Param("userId") Long userId, @Param("createTime") LocalDateTime createTime);

    @Delete("delete from community_like where post_id = #{postId} and user_id = #{userId}")
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);

    @Select("<script>select post_id from community_like where user_id = #{userId} and post_id in " +
            "<foreach collection='postIds' item='postId' open='(' separator=',' close=')'>#{postId}</foreach></script>")
    List<Long> listLikedPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);
}