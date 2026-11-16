package com.pet.service.impl;

import com.pet.constant.MessageConstant;
import com.pet.entity.Store;
import com.pet.exception.BusinessException;
import com.pet.mapper.StoreMapper;
import com.pet.service.CatalogCacheService;
import com.pet.service.StoreService;
import com.pet.vo.NearbyStoreVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 门店业务实现。
 *
 * <p>附近门店优先走 Redis GEO 半径查询，索引为空时自动重建，Redis 异常时
 * 回退数据库并用 Haversine 公式计算距离。</p>
 */
@Service
@Slf4j
public class StoreServiceImpl implements StoreService {

    private static final String GEO_KEY = "pet:store:geo";

    @Autowired
    private StoreMapper storeMapper;
    @Autowired
    private CatalogCacheService catalogCacheService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 查询营业中的门店。
     */
    @Override
    public List<Store> listActive() {
        return storeMapper.listActive();
    }

    /**
     * 查询全部门店，供管理端维护。
     */
    @Override
    public List<Store> listAll() {
        return storeMapper.listAll();
    }

    /**
     * 查询门店详情，不存在时抛出业务异常。
     */
    @Override
    public Store getById(Long id) {
        Store store = storeMapper.getById(id);
        if (store == null) {
            throw new BusinessException(MessageConstant.STORE_NOT_FOUND);
        }
        return store;
    }

    /**
     * 修改门店：校验必填项后更新数据库，清理门店缓存并重建 GEO 索引。
     */
    @Override
    public void update(Store store) {
        if (store == null || store.getId() == null
                || store.getName() == null || store.getName().trim().isEmpty()
                || store.getAddress() == null || store.getAddress().trim().isEmpty()) {
            throw new BusinessException(MessageConstant.STORE_NAME_REQUIRED);
        }
        store.setUpdateTime(LocalDateTime.now());
        if (store.getStatus() == null) {
            store.setStatus(1);
        }
        if (storeMapper.update(store) == 0) {
            throw new BusinessException(MessageConstant.STORE_NOT_FOUND);
        }
        catalogCacheService.evictByPrefix("pet:catalog:store:");
        syncGeo();
    }

    /**
     * 附近门店：优先使用 Redis GEO 半径查询并按距离升序，
     * 无结果或异常时回退数据库计算。
     */
    @Override
    public List<NearbyStoreVO> nearby(double longitude, double latitude, double radiusKm, int limit) {
        double safeRadius = radiusKm <= 0 ? 5 : radiusKm;
        int safeLimit = limit <= 0 ? 10 : limit;
        try {
            Long size = stringRedisTemplate.opsForZSet().zCard(GEO_KEY);
            if (size == null || size == 0) {
                syncGeo();
            }
            GeoOperations<String, String> geo = stringRedisTemplate.opsForGeo();
            GeoResults<RedisGeoCommands.GeoLocation<String>> results = geo.radius(
                    GEO_KEY,
                    new Circle(new Point(longitude, latitude), new Distance(safeRadius, Metrics.KILOMETERS)),
                    RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                            .includeDistance().sortAscending().limit(safeLimit));
            List<NearbyStoreVO> stores = new ArrayList<>();
            if (results != null) {
                for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                    Store store = storeMapper.getById(Long.valueOf(result.getContent().getName()));
                    if (store != null && Integer.valueOf(1).equals(store.getStatus())) {
                        stores.add(toVO(store, result.getDistance().getValue() * 1000));
                    }
                }
            }
            if (!stores.isEmpty()) {
                return stores;
            }
        } catch (Exception e) {
            log.warn("Redis GEO 查询失败，回退数据库：{}", e.getMessage());
        }
        return nearbyFromDatabase(longitude, latitude, safeRadius, safeLimit);
    }

    /**
     * 重建 GEO 索引：清空旧 key 后写入所有有经纬度的营业门店。
     */
    @Override
    public int syncGeo() {
        List<Store> stores = storeMapper.listActive();
        if (stores.isEmpty()) {
            return 0;
        }
        try {
            stringRedisTemplate.delete(GEO_KEY);
            GeoOperations<String, String> geo = stringRedisTemplate.opsForGeo();
            int count = 0;
            for (Store store : stores) {
                if (store.getLongitude() == null || store.getLatitude() == null) {
                    continue;
                }
                geo.add(GEO_KEY, new Point(store.getLongitude(), store.getLatitude()),
                        String.valueOf(store.getId()));
                count++;
            }
            log.info("门店 GEO 索引同步完成，共 {} 家", count);
            return count;
        } catch (Exception e) {
            log.warn("门店 GEO 索引同步失败：{}", e.getMessage());
            return 0;
        }
    }

    /**
     * GEO 不可用时的数据库兜底：逐店计算球面距离并过滤、排序、截断。
     */
    private List<NearbyStoreVO> nearbyFromDatabase(double longitude, double latitude,
                                                   double radiusKm, int limit) {
        List<NearbyStoreVO> result = new ArrayList<>();
        for (Store store : storeMapper.listActive()) {
            if (store.getLongitude() == null || store.getLatitude() == null) {
                continue;
            }
            double meters = haversineMeters(latitude, longitude, store.getLatitude(), store.getLongitude());
            if (meters <= radiusKm * 1000) {
                result.add(toVO(store, meters));
            }
        }
        result.sort(Comparator.comparing(NearbyStoreVO::getDistance));
        if (result.size() > limit) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    /**
     * 组装附近门店展示对象，距离单位为米。
     */
    private NearbyStoreVO toVO(Store store, double meters) {
        return NearbyStoreVO.builder()
                .id(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .longitude(store.getLongitude())
                .latitude(store.getLatitude())
                .distance(Math.round(meters * 10) / 10.0)
                .build();
    }

    /**
     * Haversine 公式计算两点间球面距离（米）。
     */
    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return earthRadius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}