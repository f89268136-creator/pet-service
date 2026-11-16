package com.pet.mapper;

import com.pet.entity.ServicePackageItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 服务套餐与项目关联数据访问接口。
 */
@Mapper
public interface ServicePackageItemMapper {
    /**
     * 根据服务项目id查询对应的服务套餐id
     *
     * @param dishIds
     * @return
     */
    //select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量保存服务套餐和服务项目的关联关系
     *
     * @param setmealDishes
     */
    void insertBatch(List<ServicePackageItem> setmealDishes);

    /**
     * 根据服务套餐id删除服务套餐和服务项目的关联关系
     *
     * @param setmealId
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
