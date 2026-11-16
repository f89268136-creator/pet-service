package com.pet.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pet.constant.MessageConstant;
import com.pet.constant.StatusConstant;
import com.pet.dto.ServiceItemDTO;
import com.pet.dto.ServiceItemPageQueryDTO;
import com.pet.entity.ServiceItem;
import com.pet.entity.ServicePackage;
import com.pet.exception.DeletionNotAllowedException;
import com.pet.mapper.ServiceItemMapper;
import com.pet.mapper.ServicePackageItemMapper;
import com.pet.mapper.ServicePackageMapper;
import com.pet.result.PageResult;
import com.pet.service.ServiceItemService;
import com.pet.vo.ServiceItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务项目业务实现。
 *
 * <p>负责服务项目的增删改查与上下架，删除前会校验上架状态及套餐关联。</p>
 */
@Service
@Slf4j
public class ServiceItemServiceImpl implements ServiceItemService {

    @Autowired
    private ServiceItemMapper dishMapper;
    @Autowired
    private ServicePackageItemMapper setmealDishMapper;
    @Autowired
    private ServicePackageMapper setmealMapper;

    /**
     * 新增服务项目。
     */
    @Transactional
    public void save(ServiceItemDTO dishDTO) {
        ServiceItem dish = new ServiceItem();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.insert(dish);
    }

    /**
     * 分页查询服务项目。
     */
    public PageResult pageQuery(ServiceItemPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<ServiceItemVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除服务项目：上架中或已被套餐引用的项目不允许删除。
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) {
            ServiceItem dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SERVICE_ITEM_ON_SALE);
            }
        }

        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SERVICE_ITEM_BE_RELATED_BY_PACKAGE);
        }

        for (Long id : ids) {
            dishMapper.deleteById(id);
        }
    }

    /**
     * 根据 ID 查询服务项目。
     */
    public ServiceItemVO getById(Long id) {
        ServiceItem dish = dishMapper.getById(id);
        ServiceItemVO dishVO = new ServiceItemVO();
        BeanUtils.copyProperties(dish, dishVO);
        return dishVO;
    }

    /**
     * 修改服务项目。
     */
    public void update(ServiceItemDTO dishDTO) {
        ServiceItem dish = new ServiceItem();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
    }

    /**
     * 上架或下架服务项目；下架时同步下架关联该项目的套餐。
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        ServiceItem dish = ServiceItem.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    ServicePackage setmeal = ServicePackage.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * 查询指定分类下已上架的服务项目。
     */
    public List<ServiceItem> list(Long categoryId) {
        ServiceItem dish = ServiceItem.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 按条件查询服务项目并转换为展示对象。
     */
    public List<ServiceItemVO> listVO(ServiceItem dish) {
        List<ServiceItem> dishList = dishMapper.list(dish);
        List<ServiceItemVO> dishVOList = new ArrayList<>();
        for (ServiceItem d : dishList) {
            ServiceItemVO dishVO = new ServiceItemVO();
            BeanUtils.copyProperties(d, dishVO);
            dishVOList.add(dishVO);
        }
        return dishVOList;
    }
}
