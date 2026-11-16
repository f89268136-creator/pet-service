package com.pet.mapper;

import com.pet.entity.CommunityPost;
import com.pet.vo.CommunityPostVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 社区动态数据访问接口。
 *
 * <p>提供信息流查询、互动计数更新和状态维护。</p>
 */
@Mapper
public interface CommunityPostMapper {

    @Insert("insert into community_post(user_id, pet_id, store_id, content, images, like_count, comment_count, status, create_time, update_time) " +
            "values(#{userId}, #{petId}, #{storeId}, #{content}, #{images}, 0, 0, 1, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommunityPost post);

    @Select("select * from community_post where id = #{id}")
    CommunityPost getById(Long id);

    @Results(id = "communityPostVOResult", value = {
            @Result(column = "images", property = "imagesJson")
    })
    @Select("<script>select p.*, u.name as user_name, u.avatar as user_avatar, " +
            "pet.name as pet_name, pet.species as pet_species, s.name as store_name " +
            "from community_post p " +
            "left join user u on p.user_id = u.id " +
            "left join pet on p.pet_id = pet.id " +
            "left join store s on p.store_id = s.id <where>" +
            "<if test='status != null'>and p.status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'>and p.content like concat('%', #{keyword}, '%')</if>" +
            "</where> order by p.create_time desc</script>")
    List<CommunityPostVO> pageVO(@Param("status") Integer status, @Param("keyword") String keyword);

    @ResultMap("communityPostVOResult")
    @Select("select p.*, u.name as user_name, u.avatar as user_avatar, " +
            "pet.name as pet_name, pet.species as pet_species, s.name as store_name " +
            "from community_post p " +
            "left join user u on p.user_id = u.id " +
            "left join pet on p.pet_id = pet.id " +
            "left join store s on p.store_id = s.id " +
            "where p.status = 1 order by p.create_time desc")
    List<CommunityPostVO> listVisibleVOByTime();

    @ResultMap("communityPostVOResult")
    @Select("select p.*, u.name as user_name, u.avatar as user_avatar, " +
            "pet.name as pet_name, pet.species as pet_species, s.name as store_name " +
            "from community_post p " +
            "left join user u on p.user_id = u.id " +
            "left join pet on p.pet_id = pet.id " +
            "left join store s on p.store_id = s.id " +
            "where p.status = 1 order by p.like_count desc, p.comment_count desc, p.create_time desc")
    List<CommunityPostVO> listVisibleVOByHot();

    @ResultMap("communityPostVOResult")
    @Select("<script>select p.*, u.name as user_name, u.avatar as user_avatar, " +
            "pet.name as pet_name, pet.species as pet_species, s.name as store_name " +
            "from community_post p " +
            "left join user u on p.user_id = u.id " +
            "left join pet on p.pet_id = pet.id " +
            "left join store s on p.store_id = s.id where p.id in " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>")
    List<CommunityPostVO> listVOByIds(@Param("ids") List<Long> ids);

    @Update("update community_post set status = #{status}, update_time = #{updateTime} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") java.time.LocalDateTime updateTime);

    @Update("update community_post set like_count = greatest(like_count + #{delta}, 0) where id = #{id}")
    int incrementLike(@Param("id") Long id, @Param("delta") int delta);

    @Update("update community_post set comment_count = greatest(comment_count + #{delta}, 0) where id = #{id}")
    int incrementComment(@Param("id") Long id, @Param("delta") int delta);

    @Select("select count(1) from community_post where status = 1")
    int countVisible();
}