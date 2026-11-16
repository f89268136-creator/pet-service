package com.pet.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pet.constant.MessageConstant;
import com.pet.constant.StatusConstant;
import com.pet.dto.ServicePackageDTO;
import com.pet.dto.ServicePackagePageQueryDTO;
import com.pet.entity.ServiceItem;
import com.pet.entity.ServicePackage;
import com.pet.entity.ServicePackageItem;
import com.pet.exception.DeletionNotAllowedException;
import com.pet.exception.ServicePackageEnableFailedException;
import com.pet.mapper.ServiceItemMapper;
import com.pet.mapper.ServicePackageItemMapper;
import com.pet.mapper.ServicePackageMapper;
import com.pet.result.PageResult;
import com.pet.service.ServicePackageService;
import com.pet.vo.ServicePackageItemVO;
import com.pet.vo.ServicePackageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 服务套餐业务实现
 */
@Service
@Slf4j
public class ServicePackageServiceImpl implements ServicePackageService {

    @Autowired
    private ServicePackageMapper setmealMapper;
    @Autowired
    private ServicePackageItemMapper setmealDishMapper;
    @Autowired
    private ServiceItemMapper dishMapper;

    /**
     * 新增服务套餐，同时需要保存服务套餐和服务项目的关联关系
     *
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(ServicePackageDTO setmealDTO) {
        ServicePackage setmeal = new ServicePackage();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //向服务套餐表插入数据
        setmealMapper.insert(setmeal);

        //获取生成的服务套餐id
        Long setmealId = setmeal.getId();

        List<ServicePackageItem> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        //保存服务套餐和服务项目的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(ServicePackagePageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        Page<ServicePackageVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除服务套餐
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            ServicePackage setmeal = setmealMapper.getById(id);
            if (StatusConstant.ENABLE == setmeal.getStatus()) {
                //起售中的服务套餐不能删除
                throw new DeletionNotAllowedException(MessageConstant.PACKAGE_ON_SALE);
            }
        });

        ids.forEach(setmealId -> {
            //删除服务套餐表中的数据
            setmealMapper.deleteById(setmealId);
            //删除服务套餐服务项目关系表中的数据
            setmealDishMapper.deleteBySetmealId(setmealId);
        });
    }

    /**
     * 根据id查询服务套餐和服务套餐服务项目关系
     *
     * @param id
     * @return
     */
    public ServicePackageVO getByIdWithDish(Long id) {
        ServicePackageVO setmealVO = setmealMapper.getByIdWithDish(id);
        return setmealVO;
    }

    /**
     * 修改服务套餐
     *
     * @param setmealDTO
     */
    @Transactional
    public void update(ServicePackageDTO setmealDTO) {
        ServicePackage setmeal = new ServicePackage();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //1、修改服务套餐表，执行update
        setmealMapper.update(setmeal);

        //服务套餐id
        Long setmealId = setmealDTO.getId();

        //2、删除服务套餐和服务项目的关联关系，操作setmeal_dish表，执行delete
        setmealDishMapper.deleteBySetmealId(setmealId);

        List<ServicePackageItem> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        //3、重新插入服务套餐和服务项目的关联关系，操作setmeal_dish表，执行insert
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 服务套餐起售、停售
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        //起售服务套餐时，判断服务套餐内是否有停售服务项目，有停售服务项目提示"服务套餐内包含未启售服务项目，无法启售"
        if (status == StatusConstant.ENABLE) {
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<ServiceItem> dishList = dishMapper.getBySetmealId(id);
            if (dishList != null && dishList.size() > 0) {
                dishList.forEach(dish -> {
                    if (StatusConstant.DISABLE == dish.getStatus()) {
                        throw new ServicePackageEnableFailedException(MessageConstant.PACKAGE_ENABLE_FAILED);
                    }
                });
            }
        }

        ServicePackage setmeal = ServicePackage.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<ServicePackage> list(ServicePackage setmeal) {
        List<ServicePackage> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询服务项目选项
     * @param id
     * @return
     */
    public List<ServicePackageItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
