package com.pet.controller.user;

import com.pet.context.BaseContext;
import com.pet.dto.CommunityCommentDTO;
import com.pet.dto.CommunityPostDTO;
import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.CommunityService;
import com.pet.vo.CommunityCommentVO;
import com.pet.vo.CommunityPostVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端宠物社区接口。
 *
 * <p>提供信息流浏览、发布动态、点赞评论和动态详情查询。</p>
 */
@RestController("userCommunityController")
@RequestMapping("/user/community")
@Api(tags = "用户端宠物社区接口")
@Slf4j
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    /**
     * 分页浏览社区信息流，sort 支持 time（时间序）和 hot（热度序）。
     */
    @GetMapping("/feed")
    @ApiOperation("社区信息流")
    public Result<PageResult> feed(@RequestParam(defaultValue = "time") String sort,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(communityService.feed(sort, page, pageSize, BaseContext.getCurrentId()));
    }

    /**
     * 发布一条宠物动态。
     */
    @PostMapping
    @ApiOperation("发布动态")
    public Result<CommunityPostVO> publish(@RequestBody CommunityPostDTO dto) {
        return Result.success(communityService.publish(dto, BaseContext.getCurrentId()));
    }

    /**
     * 查询动态详情。
     */
    @GetMapping("/{id}")
    @ApiOperation("动态详情")
    public Result<CommunityPostVO> detail(@PathVariable Long id) {
        return Result.success(communityService.detail(id, BaseContext.getCurrentId()));
    }

    /**
     * 点赞或取消点赞，返回当前点赞状态。
     */
    @PostMapping("/{id}/like")
    @ApiOperation("点赞或取消点赞")
    public Result<Boolean> like(@PathVariable Long id) {
        return Result.success(communityService.toggleLike(id, BaseContext.getCurrentId()));
    }

    /**
     * 查询动态的评论列表。
     */
    @GetMapping("/{id}/comments")
    @ApiOperation("动态评论列表")
    public Result<List<CommunityCommentVO>> comments(@PathVariable Long id) {
        return Result.success(communityService.comments(id));
    }

    /**
     * 发表评论并返回最新评论列表。
     */
    @PostMapping("/{id}/comment")
    @ApiOperation("发表评论")
    public Result<List<CommunityCommentVO>> comment(@PathVariable Long id,
                                                    @RequestBody CommunityCommentDTO dto) {
        return Result.success(communityService.comment(id, dto.getContent(), BaseContext.getCurrentId()));
    }
}