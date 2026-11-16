package com.pet.service;

import com.pet.dto.ServiceItemDTO;
import com.pet.dto.ServiceItemPageQueryDTO;
import com.pet.entity.ServiceItem;
import com.pet.result.PageResult;
import com.pet.vo.ServiceItemVO;

import java.util.List;

/**
 * 服务项目业务接口。
 */
public interface ServiceItemService {

    /**
     * 新增服务项目
     *
     * @param dishDTO
     */
    void save(ServiceItemDTO dishDTO);

    /**
     * 服务项目分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(ServiceItemPageQueryDTO dishPageQueryDTO);

    /**
     * 服务项目批量删除
     *
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询服务项目
     *
     * @param id
     * @return
     */
    ServiceItemVO getById(Long id);

    /**
     * 根据id修改服务项目
     *
     * @param dishDTO
     */
    void update(ServiceItemDTO dishDTO);

    /**
     * 服务项目起售停售
     *
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询服务项目
     *
     * @param categoryId
     * @return
     */
    List<ServiceItem> list(Long categoryId);

    /**
     * 条件查询服务项目
     * @param dish
     * @return
     */
    List<ServiceItemVO> listVO(ServiceItem dish);
}
