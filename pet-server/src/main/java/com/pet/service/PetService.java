package com.pet.service;

import com.pet.dto.PetDTO;
import com.pet.entity.Pet;

import java.util.List;

/**
 * 宠物档案业务接口。
 *
 * <p>所有操作均限定在当前登录用户名下，存在进行中预约的宠物不允许删除。</p>
 */
public interface PetService {

    /**
     * 查询当前用户的宠物列表。
     */
    List<Pet> listMine();

    /**
     * 新增宠物档案。
     */
    Pet add(PetDTO petDTO);

    /**
     * 修改宠物档案。
     */
    void update(Long id, PetDTO petDTO);

    /**
     * 删除宠物档案。
     */
    void delete(Long id);
}
