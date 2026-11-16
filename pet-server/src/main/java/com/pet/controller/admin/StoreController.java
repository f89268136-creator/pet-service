package com.pet.controller.admin;

import com.pet.entity.Store;
import com.pet.result.Result;
import com.pet.service.StoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端门店管理接口。
 *
 * <p>提供门店列表、门店信息修改和 GEO 索引重建。</p>
 */
@RestController
@RequestMapping("/admin/store")
@Api(tags = "管理端门店管理接口")
@Slf4j
public class StoreController {

    @Autowired
    private StoreService storeService;

    /**
     * 查询全部门店（含停业门店）。
     */
    @GetMapping("/list")
    @ApiOperation("门店列表")
    public Result<List<Store>> list() {
        return Result.success(storeService.listAll());
    }

    /**
     * 修改门店信息，成功后清理缓存并同步 GEO 索引。
     */
    @PutMapping
    @ApiOperation("修改门店")
    public Result update(@RequestBody Store store) {
        storeService.update(store);
        return Result.success();
    }

    /**
     * 重建门店 GEO 索引。
     */
    @PostMapping("/sync-geo")
    @ApiOperation("重建门店 GEO 索引")
    public Result<Integer> syncGeo() {
        return Result.success(storeService.syncGeo());
    }
}