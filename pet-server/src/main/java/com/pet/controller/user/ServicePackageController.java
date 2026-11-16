package com.pet.controller.user;

import com.pet.constant.StatusConstant;
import com.pet.entity.ServicePackage;
import com.pet.result.Result;
import com.pet.service.CatalogCacheService;
import com.pet.service.ServicePackageService;
import com.pet.vo.ServicePackageItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户端服务套餐浏览接口。
 */
@RestController("userServicePackageController")
@RequestMapping("/user/service-package")
@Api(tags = "C端-服务套餐浏览接口")
public class ServicePackageController {
    @Autowired
    private ServicePackageService servicePackageService;
    @Autowired
    private CatalogCacheService catalogCacheService;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询服务套餐")
    public Result<List<ServicePackage>> list(Long categoryId) {
        String key = "pet:catalog:service-package:" + (categoryId == null ? "all" : categoryId);
        List<ServicePackage> list = catalogCacheService.getList(key, ServicePackage.class, () -> {
            ServicePackage servicePackage = new ServicePackage();
            servicePackage.setCategoryId(categoryId);
            servicePackage.setStatus(StatusConstant.ENABLE);
            return servicePackageService.list(servicePackage);
        });
        return Result.success(list);
    }

    /**
     * 根据服务套餐id查询包含的服务项目列表
     *
     * @param id
     * @return
     */
    @GetMapping("/items/{id}")
    @ApiOperation("根据服务套餐id查询包含的服务项目列表")
    public Result<List<ServicePackageItemVO>> dishList(@PathVariable("id") Long id) {
        List<ServicePackageItemVO> list = servicePackageService.getDishItemById(id);
        return Result.success(list);
    }
}