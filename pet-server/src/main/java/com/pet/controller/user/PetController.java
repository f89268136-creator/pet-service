package com.pet.controller.user;

import com.pet.dto.PetDTO;
import com.pet.entity.Pet;
import com.pet.result.Result;
import com.pet.service.PetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端宠物档案接口。
 *
 * <p>提供当前用户宠物档案的增删改查，删除时会检查是否存在进行中的预约。</p>
 */
@RestController("userPetController")
@RequestMapping("/user/pet")
@Api(tags = "用户端宠物档案接口")
@Slf4j
public class PetController {

    @Autowired
    private PetService petService;

    /**
     * 查询当前用户的宠物列表。
     */
    @GetMapping("/list")
    @ApiOperation("查询我的宠物")
    public Result<List<Pet>> list() {
        return Result.success(petService.listMine());
    }

    /**
     * 新增宠物档案。
     */
    @PostMapping
    @ApiOperation("新增宠物")
    public Result<Pet> add(@RequestBody PetDTO petDTO) {
        log.info("新增宠物：{}", petDTO);
        return Result.success(petService.add(petDTO));
    }

    /**
     * 修改宠物档案。
     */
    @PutMapping("/{id}")
    @ApiOperation("修改宠物")
    public Result update(@PathVariable Long id, @RequestBody PetDTO petDTO) {
        petService.update(id, petDTO);
        return Result.success();
    }

    /**
     * 删除宠物档案，存在进行中预约时会被拒绝。
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除宠物")
    public Result delete(@PathVariable Long id) {
        petService.delete(id);
        return Result.success();
    }
}
