package com.pet.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pet.constant.MessageConstant;
import com.pet.dto.CommunityPostDTO;
import com.pet.entity.CommunityComment;
import com.pet.entity.CommunityPost;
import com.pet.entity.Pet;
import com.pet.exception.BusinessException;
import com.pet.mapper.CommunityCommentMapper;
import com.pet.mapper.CommunityLikeMapper;
import com.pet.mapper.CommunityPostMapper;
import com.pet.mapper.PetMapper;
import com.pet.result.PageResult;
import com.pet.service.CommunityService;
import com.pet.vo.CommunityCommentVO;
import com.pet.vo.CommunityPostVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 宠物社区业务实现。
 *
 * <p>信息流使用 Redis ZSet 分别维护时间序与热度序，分页时按 ZSet 排名回表组装；
 * Redis 不可用时自动回退数据库排序，发布/点赞/评论会同步刷新热度分。</p>
 */
@Service
@Slf4j
public class CommunityServiceImpl implements CommunityService {

    public static final String FEED_TIME_KEY = "pet:community:feed:time";
    public static final String FEED_HOT_KEY = "pet:community:feed:hot";
    private static final int FEED_MAX_SIZE = 500;

    @Autowired
    private CommunityPostMapper postMapper;
    @Autowired
    private CommunityCommentMapper commentMapper;
    @Autowired
    private CommunityLikeMapper likeMapper;
    @Autowired
    private PetMapper petMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 社区信息流：按时间或热度排序，优先读取 Redis ZSet 分页，
     * 缓存为空时自动预热，异常时回退数据库查询。
     */
    @Override
    public PageResult feed(String sort, int page, int pageSize, Long userId) {
        int safePage = page <= 0 ? 1 : page;
        int safeSize = pageSize <= 0 ? 10 : pageSize;
        boolean hot = "hot".equalsIgnoreCase(sort);
        long offset = (long) (safePage - 1) * safeSize;
        long end = offset + safeSize - 1;

        String key = hot ? FEED_HOT_KEY : FEED_TIME_KEY;
        try {
            Long total = stringRedisTemplate.opsForZSet().zCard(key);
            if (total == null || total == 0) {
                warmFeed();
                total = stringRedisTemplate.opsForZSet().zCard(key);
            }
            if (total != null && total > 0) {
                Set<String> ids = stringRedisTemplate.opsForZSet().reverseRange(key, offset, end);
                List<CommunityPostVO> posts = loadOrdered(ids, userId);
                return new PageResult(total, posts);
            }
        } catch (Exception e) {
            log.warn("社区信息流读取 Redis 失败，回退数据库：{}", e.getMessage());
        }

        PageHelper.startPage(safePage, safeSize);
        Page<CommunityPostVO> result = (Page<CommunityPostVO>) (hot
                ? postMapper.listVisibleVOByHot() : postMapper.listVisibleVOByTime());
        List<CommunityPostVO> posts = new ArrayList<>(result.getResult());
        enrich(posts, userId);
        return new PageResult(result.getTotal(), posts);
    }

    /**
     * 发布动态：校验内容与宠物归属，写入数据库后同步加入信息流。
     */
    @Override
    public CommunityPostVO publish(CommunityPostDTO dto, Long userId) {
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException(MessageConstant.COMMUNITY_CONTENT_REQUIRED);
        }
        if (dto.getPetId() != null) {
            Pet pet = petMapper.getById(dto.getPetId());
            if (pet == null) {
                throw new BusinessException(MessageConstant.PET_NOT_FOUND);
            }
            if (!pet.getUserId().equals(userId)) {
                throw new BusinessException(MessageConstant.PET_ACCESS_DENIED);
            }
        }
        String images = dto.getImages() == null || dto.getImages().isEmpty()
                ? null : JSON.toJSONString(dto.getImages());
        LocalDateTime now = LocalDateTime.now();
        CommunityPost post = CommunityPost.builder()
                .userId(userId)
                .petId(dto.getPetId())
                .storeId(dto.getStoreId())
                .content(dto.getContent().trim())
                .images(images)
                .likeCount(0)
                .commentCount(0)
                .status(1)
                .createTime(now)
                .updateTime(now)
                .build();
        postMapper.insert(post);
        addToFeed(post);
        return loadOrdered(Collections.singleton(String.valueOf(post.getId())), userId).get(0);
    }

    /**
     * 查询可见动态详情，并补齐图片、点赞状态等展示字段。
     */
    @Override
    public CommunityPostVO detail(Long id, Long userId) {
        CommunityPost post = postMapper.getById(id);
        if (post == null || !Integer.valueOf(1).equals(post.getStatus())) {
            throw new BusinessException(MessageConstant.COMMUNITY_POST_NOT_FOUND);
        }
        List<CommunityPostVO> list = loadOrdered(Collections.singleton(String.valueOf(id)), userId);
        if (list.isEmpty()) {
            throw new BusinessException(MessageConstant.COMMUNITY_POST_NOT_FOUND);
        }
        return list.get(0);
    }

    /**
     * 查询某条动态的评论列表。
     */
    @Override
    public List<CommunityCommentVO> comments(Long postId) {
        checkVisiblePost(postId);
        return commentMapper.listByPostId(postId);
    }

    /**
     * 发表评论：写入评论、递增动态评论数并刷新热度分。
     */
    @Override
    public List<CommunityCommentVO> comment(Long postId, String content, Long userId) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(MessageConstant.COMMUNITY_COMMENT_REQUIRED);
        }
        CommunityPost post = checkVisiblePost(postId);
        CommunityComment comment = CommunityComment.builder()
                .postId(postId)
                .userId(userId)
                .content(content.trim())
                .createTime(LocalDateTime.now())
                .build();
        commentMapper.insert(comment);
        postMapper.incrementComment(postId, 1);
        refreshHotScore(postId);
        return commentMapper.listByPostId(postId);
    }

    /**
     * 点赞/取消点赞，返回操作后的点赞状态。
     */
    @Override
    public boolean toggleLike(Long postId, Long userId) {
        CommunityPost post = checkVisiblePost(postId);
        boolean liked;
        if (likeMapper.exists(postId, userId) > 0) {
            likeMapper.delete(postId, userId);
            postMapper.incrementLike(postId, -1);
            liked = false;
        } else {
            likeMapper.insert(postId, userId, LocalDateTime.now());
            postMapper.incrementLike(postId, 1);
            liked = true;
        }
        refreshHotScore(postId);
        return liked;
    }

    /**
     * 管理端分页查询社区动态，可按状态和关键字筛选。
     */
    @Override
    public PageResult pageAdmin(int page, int pageSize, Integer status, String keyword) {
        PageHelper.startPage(page <= 0 ? 1 : page, pageSize <= 0 ? 10 : pageSize);
        Page<CommunityPostVO> result = (Page<CommunityPostVO>) postMapper.pageVO(status, keyword);
        List<CommunityPostVO> posts = new ArrayList<>(result.getResult());
        enrich(posts, null);
        return new PageResult(result.getTotal(), posts);
    }

    /**
     * 上架或下架动态：上架加入信息流，下架从两个 ZSet 中移除。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        CommunityPost post = postMapper.getById(id);
        if (post == null) {
            throw new BusinessException(MessageConstant.COMMUNITY_POST_NOT_FOUND);
        }
        postMapper.updateStatus(id, status, LocalDateTime.now());
        post.setStatus(status);
        if (Integer.valueOf(1).equals(status)) {
            addToFeed(post);
        } else {
            try {
                stringRedisTemplate.opsForZSet().remove(FEED_TIME_KEY, String.valueOf(id));
                stringRedisTemplate.opsForZSet().remove(FEED_HOT_KEY, String.valueOf(id));
            } catch (Exception e) {
                log.warn("社区动态下架后清理信息流失败，postId={}：{}", id, e.getMessage());
            }
        }
    }

    /**
     * 校验动态存在且处于可见状态。
     */
    private CommunityPost checkVisiblePost(Long postId) {
        CommunityPost post = postMapper.getById(postId);
        if (post == null || !Integer.valueOf(1).equals(post.getStatus())) {
            throw new BusinessException(MessageConstant.COMMUNITY_POST_NOT_FOUND);
        }
        return post;
    }

    /**
     * 按 ZSet 给出的顺序回表组装动态，保证分页结果与排名一致。
     */
    private List<CommunityPostVO> loadOrdered(Set<String> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> postIds = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        List<CommunityPostVO> posts = postMapper.listVOByIds(postIds);
        Map<Long, CommunityPostVO> indexed = new HashMap<>();
        for (CommunityPostVO post : posts) {
            indexed.put(post.getId(), post);
        }
        LinkedHashSet<String> orderedIds = new LinkedHashSet<>(ids);
        List<CommunityPostVO> ordered = new ArrayList<>();
        for (String id : orderedIds) {
            CommunityPostVO post = indexed.get(Long.valueOf(id));
            if (post != null) {
                ordered.add(post);
            }
        }
        enrich(ordered, userId);
        return ordered;
    }

    /**
     * 补齐动态的图片列表和当前用户点赞状态。
     */
    private void enrich(List<CommunityPostVO> posts, Long userId) {
        if (posts.isEmpty()) {
            return;
        }
        List<Long> postIds = posts.stream().map(CommunityPostVO::getId).collect(Collectors.toList());
        Set<Long> likedIds = new java.util.HashSet<>();
        if (userId != null) {
            try {
                likedIds.addAll(likeMapper.listLikedPostIds(userId, postIds));
            } catch (Exception e) {
                log.warn("查询点赞状态失败：{}", e.getMessage());
            }
        }
        for (CommunityPostVO post : posts) {
            if (post.getImagesJson() != null && !post.getImagesJson().trim().isEmpty()) {
                try {
                    post.setImages(JSON.parseArray(post.getImagesJson(), String.class));
                } catch (Exception e) {
                    post.setImages(Collections.emptyList());
                }
            } else {
                post.setImages(Collections.emptyList());
            }
            post.setImagesJson(null);
            post.setLiked(likedIds.contains(post.getId()));
        }
    }

    /**
     * 信息流为空时，从数据库加载可见动态预热两个 ZSet。
     */
    private void warmFeed() {
        List<CommunityPostVO> posts = postMapper.listVisibleVOByTime();
        for (CommunityPostVO vo : posts) {
            CommunityPost post = new CommunityPost();
            post.setId(vo.getId());
            post.setCreateTime(vo.getCreateTime());
            post.setLikeCount(vo.getLikeCount());
            post.setCommentCount(vo.getCommentCount());
            addToFeed(post);
        }
        log.info("社区信息流预热完成，共 {} 条动态", posts.size());
    }

    /**
     * 将动态写入时间序与热度序 ZSet，并裁剪到上限条数。
     */
    private void addToFeed(CommunityPost post) {
        try {
            String member = String.valueOf(post.getId());
            long millis = post.getCreateTime() == null ? System.currentTimeMillis()
                    : post.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            ZSetOperations<String, String> zSet = stringRedisTemplate.opsForZSet();
            zSet.add(FEED_TIME_KEY, member, millis);
            zSet.add(FEED_HOT_KEY, member, hotScore(post));
            zSet.removeRange(FEED_TIME_KEY, 0, -(FEED_MAX_SIZE + 1));
            zSet.removeRange(FEED_HOT_KEY, 0, -(FEED_MAX_SIZE + 1));
        } catch (Exception e) {
            log.warn("写入社区信息流失败，postId={}：{}", post.getId(), e.getMessage());
        }
    }

    /**
     * 互动后重新计算并写回动态热度分。
     */
    private void refreshHotScore(Long postId) {
        CommunityPost post = postMapper.getById(postId);
        if (post == null || !Integer.valueOf(1).equals(post.getStatus())) {
            return;
        }
        try {
            stringRedisTemplate.opsForZSet().add(FEED_HOT_KEY, String.valueOf(postId), hotScore(post));
        } catch (Exception e) {
            log.warn("刷新社区热度失败，postId={}：{}", postId, e.getMessage());
        }
    }

    /**
     * 热度分：点赞权重高于评论，用于热度榜排序。
     */
    private double hotScore(CommunityPost post) {
        int like = post.getLikeCount() == null ? 0 : post.getLikeCount();
        int comment = post.getCommentCount() == null ? 0 : post.getCommentCount();
        return like * 1000.0 + comment * 100.0;
    }
}