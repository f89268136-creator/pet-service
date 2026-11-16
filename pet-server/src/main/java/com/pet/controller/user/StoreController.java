package com.pet.controller.user;

import com.pet.entity.Store;
import com.pet.result.Result;
import com.pet.service.CatalogCacheService;
import com.pet.service.StoreService;
import com.pet.vo.NearbyStoreVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端门店接口。
 *
 * <p>提供营业门店列表、门店详情和基于 Redis GEO 的附近门店查询。</p>
 */
@RestController("userStoreController")
@RequestMapping("/user/store")
@Api(tags = "用户端门店接口")
public class StoreController {

    @Autowired
    private StoreService storeService;
    @Autowired
    private CatalogCacheService catalogCacheService;

    /**
     * 查询营业中的门店列表，结果走缓存。
     */
    @GetMapping("/list")
    @ApiOperation("查询营业中的门店")
    public Result<List<Store>> list() {
        List<Store> list = catalogCacheService.getList("pet:catalog:store:list", Store.class,
                storeService::listActive);
        return Result.success(list);
    }

    /**
     * 查询门店详情，结果走缓存。
     */
    @GetMapping("/{id}")
    @ApiOperation("查询门店详情")
    public Result<Store> getById(@PathVariable Long id) {
        Store store = catalogCacheService.getObject("pet:catalog:store:" + id, Store.class,
                () -> storeService.getById(id));
        return Result.success(store);
    }

    /**
     * 查询坐标附近的营业门店，默认半径 5 公里、最多 10 家。
     */
    @GetMapping("/nearby")
    @ApiOperation("附近门店（GEO）")
    public Result<List<NearbyStoreVO>> nearby(@RequestParam double longitude,
                                              @RequestParam double latitude,
                                              @RequestParam(defaultValue = "5") double radius,
                                              @RequestParam(defaultValue = "10") int limit) {
        return Result.success(storeService.nearby(longitude, latitude, radius, limit));
    }
}