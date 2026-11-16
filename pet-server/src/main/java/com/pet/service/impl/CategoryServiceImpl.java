package com.pet.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pet.constant.MessageConstant;
import com.pet.constant.StatusConstant;
import com.pet.context.BaseContext;
import com.pet.dto.CategoryDTO;
import com.pet.dto.CategoryPageQueryDTO;
import com.pet.entity.Category;
import com.pet.exception.DeletionNotAllowedException;
import com.pet.mapper.CategoryMapper;
import com.pet.mapper.ServiceItemMapper;
import com.pet.mapper.ServicePackageMapper;
import com.pet.result.PageResult;
import com.pet.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类业务层
 */
@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ServiceItemMapper dishMapper;
    @Autowired
    private ServicePackageMapper setmealMapper;

    /**
     * 新增分类
     * @param categoryDTO
     */
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        //属性拷贝
        BeanUtils.copyProperties(categoryDTO, category);

        //分类状态默认为禁用状态0
        category.setStatus(StatusConstant.DISABLE);

        //设置创建时间、修改时间、创建人、修改人
        //category.setCreateTime(LocalDateTime.now());
        //category.setUpdateTime(LocalDateTime.now());
        //category.setCreateUser(BaseContext.getCurrentId());
        //category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
    }

    /**
     * 分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        //下一条sql进行分页，自动加入limit关键字分页
        Page<Category> page = categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id删除分类
     * @param id
     */
    public void deleteById(Long id) {
        //查询当前分类是否关联了服务项目，如果关联了就抛出业务异常
        Integer count = dishMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有服务项目，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SERVICE_ITEM);
        }

        //查询当前分类是否关联了服务套餐，如果关联了就抛出业务异常
        count = setmealMapper.countByCategoryId(id);
        if(count > 0){
            //当前分类下有服务项目，不能删除
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_PACKAGE);
        }

        //删除分类数据
        categoryMapper.deleteById(id);
    }

    /**
     * 修改分类
     * @param categoryDTO
     */
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);

        //设置修改时间、修改人
        //category.setUpdateTime(LocalDateTime.now());
        //category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.update(category);
    }

    /**
     * 启用、禁用分类
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        Category category = Category.builder()
                .id(id)
                .status(status)
                //.updateTime(LocalDateTime.now())
                //.updateUser(BaseContext.getCurrentId())
                .build();
        categoryMapper.update(category);
    }

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
