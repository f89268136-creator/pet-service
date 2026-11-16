package com.pet.service;

import com.pet.dto.CommunityPostDTO;
import com.pet.result.PageResult;
import com.pet.vo.CommunityCommentVO;
import com.pet.vo.CommunityPostVO;

import java.util.List;

/**
 * 宠物社区业务接口。
 *
 * <p>提供信息流、动态发布/详情、评论、点赞以及管理端审核能力。</p>
 */
public interface CommunityService {

    /**
     * 按时间或热度分页查询社区信息流。
     */
    PageResult feed(String sort, int page, int pageSize, Long userId);

    /**
     * 发布动态。
     */
    CommunityPostVO publish(CommunityPostDTO dto, Long userId);

    /**
     * 查询动态详情。
     */
    CommunityPostVO detail(Long id, Long userId);

    /**
     * 查询动态评论列表。
     */
    List<CommunityCommentVO> comments(Long postId);

    /**
     * 发表评论并返回最新评论列表。
     */
    List<CommunityCommentVO> comment(Long postId, String content, Long userId);

    /**
     * 点赞或取消点赞，返回操作后的状态。
     */
    boolean toggleLike(Long postId, Long userId);

    /**
     * 管理端分页查询动态。
     */
    PageResult pageAdmin(int page, int pageSize, Integer status, String keyword);

    /**
     * 上架或下架动态。
     */
    void updateStatus(Long id, Integer status);
}