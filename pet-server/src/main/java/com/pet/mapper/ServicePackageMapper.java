package com.pet.mapper;

import com.github.pagehelper.Page;
import com.pet.annotation.AutoFill;
import com.pet.dto.ServicePackagePageQueryDTO;
import com.pet.entity.ServicePackage;
import com.pet.enumeration.OperationType;
import com.pet.vo.ServicePackageItemVO;
import com.pet.vo.ServicePackageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 服务套餐数据访问接口。
 */
@Mapper
public interface ServicePackageMapper {

    /**
     * 根据分类id查询服务套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 根据id修改服务套餐
     *
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(ServicePackage setmeal);

    /**
     * 新增服务套餐
     *
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(ServicePackage setmeal);

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    Page<ServicePackageVO> pageQuery(ServicePackagePageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询服务套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    ServicePackage getById(Long id);

    /**
     * 根据id删除服务套餐
     * @param setmealId
     */
    @Delete("delete from setmeal where id = #{id}")
    void deleteById(Long setmealId);

    /**
     * 根据id查询服务套餐和服务套餐服务项目关系
     * @param id
     * @return
     */
    ServicePackageVO getByIdWithDish(Long id);

    /**
     * 动态条件查询服务套餐
     * @param setmeal
     * @return
     */
    List<ServicePackage> list(ServicePackage setmeal);

    /**
     * 根据服务套餐id查询服务项目选项
     * @param setmealId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<ServicePackageItemVO> getDishItemBySetmealId(Long setmealId);

    /**
     * 根据条件统计服务套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
