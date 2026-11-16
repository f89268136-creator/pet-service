package com.pet.mapper;

import com.github.pagehelper.Page;
import com.pet.annotation.AutoFill;
import com.pet.dto.ServiceItemPageQueryDTO;
import com.pet.entity.ServiceItem;
import com.pet.enumeration.OperationType;
import com.pet.vo.ServiceItemVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 服务项目数据访问接口。
 */
@Mapper
public interface ServiceItemMapper {

    /**
     * 根据分类id查询服务项目数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入服务项目数据
     *
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(ServiceItem dish);

    /**
     * 服务项目分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    Page<ServiceItemVO> pageQuery(ServiceItemPageQueryDTO dishPageQueryDTO);

    /**
     * 根据主键查询服务项目
     *
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    ServiceItem getById(Long id);

    /**
     * 根据主键删除服务项目数据
     *
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据id动态修改服务项目数据
     *
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(ServiceItem dish);

    /**
     * 动态条件查询服务项目
     *
     * @param dish
     * @return
     */
    List<ServiceItem> list(ServiceItem dish);

    /**
     * 根据服务套餐id查询服务项目
     * @param setmealId
     * @return
     */
    @Select("select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = #{setmealId}")
    List<ServiceItem> getBySetmealId(Long setmealId);

    /**
     * 根据条件统计服务项目数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
