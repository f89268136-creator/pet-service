package com.pet.controller.admin;

import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.CommunityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端社区管理接口。
 *
 * <p>用于分页查看社区动态并执行上架/下架审核。</p>
 */
@RestController
@RequestMapping("/admin/community")
@Api(tags = "管理端社区管理接口")
@Slf4j
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    /**
     * 分页查询社区动态，可按状态和关键字筛选。
     */
    @GetMapping("/page")
    @ApiOperation("社区动态分页查询")
    public Result<PageResult> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int pageSize,
                                   Integer status, String keyword) {
        return Result.success(communityService.pageAdmin(page, pageSize, status, keyword));
    }

    /**
     * 展示或隐藏动态。
     */
    @PostMapping("/status/{status}")
    @ApiOperation("展示或隐藏动态")
    public Result updateStatus(@PathVariable Integer status, Long id) {
        communityService.updateStatus(id, status);
        return Result.success();
    }
}