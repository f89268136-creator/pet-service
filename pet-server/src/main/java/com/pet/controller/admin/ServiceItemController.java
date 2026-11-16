package com.pet.controller.admin;

import com.pet.dto.ServiceItemDTO;
import com.pet.dto.ServiceItemPageQueryDTO;
import com.pet.entity.ServiceItem;
import com.pet.result.PageResult;
import com.pet.result.Result;
import com.pet.service.CatalogCacheService;
import com.pet.service.ServiceItemService;
import com.pet.vo.ServiceItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务项目管理
 */
@RestController
@RequestMapping("/admin/service-item")
@Api(tags = "服务项目相关接口")
@Slf4j
public class ServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;
    @Autowired
    private CatalogCacheService catalogCacheService;

    /**
     * 新增服务项目
     *
     * @param serviceItemDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增服务项目")
    public Result save(@RequestBody ServiceItemDTO serviceItemDTO) {
        log.info("新增服务项目：{}", serviceItemDTO);
        serviceItemService.save(serviceItemDTO);

        catalogCacheService.evictByPrefix("pet:catalog:service-item:");
        return Result.success();
    }

    /**
     * 服务项目分页查询
     *
     * @param serviceItemPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("服务项目分页查询")
    public Result<PageResult> page(ServiceItemPageQueryDTO serviceItemPageQueryDTO) {
        log.info("服务项目分页查询:{}", serviceItemPageQueryDTO);
        PageResult pageResult = serviceItemService.pageQuery(serviceItemPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 服务项目批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("服务项目批量删除")
    public Result delete(@RequestParam List<Long> ids) {
        log.info("服务项目批量删除：{}", ids);
        serviceItemService.deleteBatch(ids);

        catalogCacheService.evictByPrefix("pet:catalog:service-item:");

        return Result.success();
    }

    /**
     * 根据id查询服务项目
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询服务项目")
    public Result<ServiceItemVO> getById(@PathVariable Long id) {
        log.info("根据id查询服务项目：{}", id);
        ServiceItemVO serviceItemVO = serviceItemService.getById(id);
        return Result.success(serviceItemVO);
    }

    /**
     * 修改服务项目
     *
     * @param serviceItemDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改服务项目")
    public Result update(@RequestBody ServiceItemDTO serviceItemDTO) {
        log.info("修改服务项目：{}", serviceItemDTO);
        serviceItemService.update(serviceItemDTO);

        catalogCacheService.evictByPrefix("pet:catalog:service-item:");

        return Result.success();
    }

    /**
     * 服务项目起售停售
     *
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("服务项目起售停售")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        serviceItemService.startOrStop(status, id);

        catalogCacheService.evictByPrefix("pet:catalog:service-item:");

        return Result.success();
    }

    /**
     * 根据分类id查询服务项目
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询服务项目")
    public Result<List<ServiceItem>> list(Long categoryId) {
        List<ServiceItem> list = serviceItemService.list(categoryId);
        return Result.success(list);
    }

}
