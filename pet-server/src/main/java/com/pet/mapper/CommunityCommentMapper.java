package com.pet.mapper;

import com.pet.entity.CommunityComment;
import com.pet.vo.CommunityCommentVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 社区评论数据访问接口。
 */
@Mapper
public interface CommunityCommentMapper {

    @Insert("insert into community_comment(post_id, user_id, content, create_time) " +
            "values(#{postId}, #{userId}, #{content}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommunityComment comment);

    @Select("select c.id, c.user_id, u.name as user_name, c.content, c.create_time " +
            "from community_comment c left join user u on c.user_id = u.id " +
            "where c.post_id = #{postId} order by c.create_time asc")
    List<CommunityCommentVO> listByPostId(Long postId);
}