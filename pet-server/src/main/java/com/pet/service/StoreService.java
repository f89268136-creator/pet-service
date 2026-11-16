package com.pet.service;

import com.pet.entity.Store;
import com.pet.vo.NearbyStoreVO;

import java.util.List;

/**
 * 门店业务接口。
 *
 * <p>提供门店查询、修改、附近门店检索以及 Redis GEO 索引同步。</p>
 */
public interface StoreService {

    /**
     * 查询营业中的门店。
     */
    List<Store> listActive();

    /**
     * 查询全部门店（管理端）。
     */
    List<Store> listAll();

    /**
     * 查询门店详情，不存在时抛出业务异常。
     */
    Store getById(Long id);

    /**
     * 修改门店信息，并清理缓存、同步 GEO 索引。
     */
    void update(Store store);

    /**
     * 查询指定坐标附近的营业门店，按距离升序返回。
     */
    List<NearbyStoreVO> nearby(double longitude, double latitude, double radiusKm, int limit);

    /**
     * 重建门店 GEO 索引，返回同步成功的门店数。
     */
    int syncGeo();
}