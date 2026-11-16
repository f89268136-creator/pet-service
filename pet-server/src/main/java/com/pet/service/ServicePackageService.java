package com.pet.service;

import com.pet.dto.ServicePackageDTO;
import com.pet.dto.ServicePackagePageQueryDTO;
import com.pet.entity.ServicePackage;
import com.pet.result.PageResult;
import com.pet.vo.ServicePackageItemVO;
import com.pet.vo.ServicePackageVO;

import java.util.List;

/**
 * 服务套餐业务接口。
 */
public interface ServicePackageService {

    /**
     * 新增服务套餐，同时需要保存服务套餐和服务项目的关联关系
     *
     * @param setmealDTO
     */
    void saveWithDish(ServicePackageDTO setmealDTO);

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(ServicePackagePageQueryDTO setmealPageQueryDTO);

    /**
     * 批量删除服务套餐
     *
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询服务套餐和关联的服务项目数据
     *
     * @param id
     * @return
     */
    ServicePackageVO getByIdWithDish(Long id);

    /**
     * 修改服务套餐
     *
     * @param setmealDTO
     */
    void update(ServicePackageDTO setmealDTO);

    /**
     * 服务套餐起售、停售
     *
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<ServicePackage> list(ServicePackage setmeal);

    /**
     * 根据id查询服务项目选项
     * @param id
     * @return
     */
    List<ServicePackageItemVO> getDishItemById(Long id);
}
