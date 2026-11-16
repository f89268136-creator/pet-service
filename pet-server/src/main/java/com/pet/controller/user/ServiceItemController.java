package com.pet.controller.user;

import com.pet.constant.StatusConstant;
import com.pet.entity.ServiceItem;
import com.pet.result.Result;
import com.pet.service.CatalogCacheService;
import com.pet.service.ServiceItemService;
import com.pet.vo.ServiceItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户端服务项目浏览接口。
 */
@RestController("userServiceItemController")
@RequestMapping("/user/service-item")
@Slf4j
@Api(tags = "C端-服务项目浏览接口")
public class ServiceItemController {
    @Autowired
    private ServiceItemService serviceItemService;
    @Autowired
    private CatalogCacheService catalogCacheService;

    /**
     * 根据分类id查询服务项目
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询服务项目")
    public Result<List<ServiceItemVO>> list(Long categoryId) {
        String key = "pet:catalog:service-item:" + (categoryId == null ? "all" : categoryId);
        List<ServiceItemVO> list = catalogCacheService.getList(key, ServiceItemVO.class, () -> {
            ServiceItem serviceItem = new ServiceItem();
            serviceItem.setCategoryId(categoryId);
            serviceItem.setStatus(StatusConstant.ENABLE);
            return serviceItemService.listVO(serviceItem);
        });
        return Result.success(list);
    }

}