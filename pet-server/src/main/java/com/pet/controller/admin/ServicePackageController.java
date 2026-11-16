package com.pet.controller.admin;

import com.pet.dto.ServicePackageDTO;
import com.pet.dto.ServicePackagePageQueryDTO;
import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.CatalogCacheService;
import com.pet.service.ServicePackageService;
import com.pet.vo.ServicePackageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务套餐管理
 */
@RestController
@RequestMapping("/admin/service-package")
@Api(tags = "服务套餐相关接口")
@Slf4j
public class ServicePackageController {

    @Autowired
    private ServicePackageService servicePackageService;
    @Autowired
    private CatalogCacheService catalogCacheService;

    /**
     * 新增服务套餐
     *
     * @param servicePackageDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增服务套餐")
    public Result save(@RequestBody ServicePackageDTO servicePackageDTO) {
        servicePackageService.saveWithDish(servicePackageDTO);
        catalogCacheService.evictByPrefix("pet:catalog:service-package:");
        return Result.success();
    }

    /**
     * 分页查询
     *
     * @param servicePackagePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(ServicePackagePageQueryDTO servicePackagePageQueryDTO) {
        PageResult pageResult = servicePackageService.pageQuery(servicePackagePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除服务套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除服务套餐")
    public Result delete(@RequestParam List<Long> ids) {
        servicePackageService.deleteBatch(ids);
        catalogCacheService.evictByPrefix("pet:catalog:service-package:");
        return Result.success();
    }

    /**
     * 根据id查询服务套餐，用于修改页面回显数据
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询服务套餐")
    public Result<ServicePackageVO> getById(@PathVariable Long id) {
        ServicePackageVO servicePackageVO = servicePackageService.getByIdWithDish(id);
        return Result.success(servicePackageVO);
    }

    /**
     * 修改服务套餐
     *
     * @param servicePackageDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改服务套餐")
    public Result update(@RequestBody ServicePackageDTO servicePackageDTO) {
        servicePackageService.update(servicePackageDTO);
        catalogCacheService.evictByPrefix("pet:catalog:service-package:");
        return Result.success();
    }

    /**
     * 服务套餐起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("服务套餐起售停售")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        servicePackageService.startOrStop(status, id);
        catalogCacheService.evictByPrefix("pet:catalog:service-package:");
        return Result.success();
    }
}
