package com.pet.service.impl;

import com.pet.constant.MessageConstant;
import com.pet.context.BaseContext;
import com.pet.dto.PetDTO;
import com.pet.entity.Pet;
import com.pet.exception.BusinessException;
import com.pet.mapper.AppointmentOrderMapper;
import com.pet.mapper.PetMapper;
import com.pet.service.PetService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 宠物档案业务实现。
 *
 * <p>所有操作均校验宠物归属；删除前会检查是否存在进行中的预约，
 * 避免破坏预约单与宠物的关联。</p>
 */
@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private AppointmentOrderMapper orderMapper;

    /**
     * 查询当前登录用户的宠物列表。
     */
    @Override
    public List<Pet> listMine() {
        return petMapper.listByUserId(BaseContext.getCurrentId());
    }

    /**
     * 新增宠物：校验名称后绑定到当前用户。
     */
    @Override
    public Pet add(PetDTO petDTO) {
        if (petDTO.getName() == null || petDTO.getName().trim().isEmpty()) {
            throw new BusinessException(MessageConstant.PET_NAME_REQUIRED);
        }
        Pet pet = new Pet();
        BeanUtils.copyProperties(petDTO, pet);
        pet.setId(null);
        pet.setUserId(BaseContext.getCurrentId());
        pet.setCreateTime(LocalDateTime.now());
        pet.setUpdateTime(LocalDateTime.now());
        petMapper.insert(pet);
        return pet;
    }

    /**
     * 修改宠物：校验归属和名称，不允许修改所属用户。
     */
    @Override
    public void update(Long id, PetDTO petDTO) {
        Pet petDB = petMapper.getById(id);
        checkOwnership(petDB);
        if (petDTO.getName() == null || petDTO.getName().trim().isEmpty()) {
            throw new BusinessException(MessageConstant.PET_NAME_REQUIRED);
        }
        Pet pet = new Pet();
        BeanUtils.copyProperties(petDTO, pet);
        pet.setId(id);
        pet.setUserId(petDB.getUserId());
        pet.setUpdateTime(LocalDateTime.now());
        petMapper.update(pet);
    }

    /**
     * 删除宠物：存在进行中预约时拒绝删除。
     */
    @Override
    public void delete(Long id) {
        Pet petDB = petMapper.getById(id);
        checkOwnership(petDB);
        Integer active = orderMapper.countActiveByPetId(id);
        if (active != null && active > 0) {
            throw new BusinessException(MessageConstant.PET_HAS_ACTIVE_APPOINTMENT);
        }
        petMapper.deleteByIdAndUserId(id, petDB.getUserId());
    }

    /**
     * 校验宠物存在且属于当前用户。
     */
    private void checkOwnership(Pet petDB) {
        if (petDB == null) {
            throw new BusinessException(MessageConstant.PET_NOT_FOUND);
        }
        if (!petDB.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BusinessException(MessageConstant.PET_ACCESS_DENIED);
        }
    }
}
