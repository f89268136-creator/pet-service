package com.pet.service.impl;

import com.pet.constant.MessageConstant;
import com.pet.entity.UserSignIn;
import com.pet.exception.BusinessException;
import com.pet.mapper.UserMapper;
import com.pet.mapper.UserSignInMapper;
import com.pet.service.SignInService;
import com.pet.vo.SignInStatsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户签到业务实现。
 *
 * <p>签到记录以数据库为准保证幂等，同时在 Redis Bitmap 中标记当月签到日用于快速统计；
 * Bitmap 异常时自动回退数据库统计。</p>
 */
@Service
@Slf4j
public class SignInServiceImpl implements SignInService {

    private static final int DAILY_POINTS = 10;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    @Autowired
    private UserSignInMapper signInMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 每日签到：当天已签到则拒绝；写入签到记录、累加积分并标记 Bitmap。
     */
    @Override
    @Transactional
    public SignInStatsVO signIn(Long userId) {
        LocalDate today = LocalDate.now();
        if (signInMapper.exists(userId, today) > 0) {
            throw new BusinessException(MessageConstant.SIGN_IN_DUPLICATED);
        }
        UserSignIn record = UserSignIn.builder()
                .userId(userId)
                .signDate(today)
                .points(DAILY_POINTS)
                .createTime(LocalDateTime.now())
                .build();
        try {
            signInMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(MessageConstant.SIGN_IN_DUPLICATED);
        }
        userMapper.addPoints(userId, DAILY_POINTS);
        markBitmap(userId, today);
        return buildStats(userId, today);
    }

    /**
     * 查询本月签到统计。
     */
    @Override
    public SignInStatsVO stats(Long userId) {
        return buildStats(userId, LocalDate.now());
    }

    /**
     * 汇总签到状态、本月签到天数、连续天数和积分。
     */
    private SignInStatsVO buildStats(Long userId, LocalDate today) {
        YearMonth month = YearMonth.from(today);
        String key = bitmapKey(userId, month);
        boolean signedToday = signInMapper.exists(userId, today) > 0;
        int signedDays = countSignedDays(userId, month, key, today);
        int continuousDays = continuousDays(userId, today);
        Integer points = userMapper.getPoints(userId);
        return SignInStatsVO.builder()
                .month(month.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .signedToday(signedToday)
                .signedDays(signedDays)
                .continuousDays(continuousDays)
                .points(points == null ? 0 : points)
                .todayPoints(signedToday ? DAILY_POINTS : 0)
                .build();
    }

    /**
     * 用 Bitmap 标记当月第 N 天已签到，并设置过期时间。
     */
    private void markBitmap(Long userId, LocalDate date) {
        try {
            String key = bitmapKey(userId, YearMonth.from(date));
            stringRedisTemplate.opsForValue().setBit(key, date.getDayOfMonth() - 1, true);
            stringRedisTemplate.expire(key, 70, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("签到 Bitmap 写入失败，userId={}：{}", userId, e.getMessage());
        }
    }

    /**
     * 优先用 Bitmap 的 bitCount 统计本月签到天数，失败时回退数据库。
     */
    private int countSignedDays(Long userId, YearMonth month, String key, LocalDate today) {
        try {
            Long count = stringRedisTemplate.execute((RedisCallback<Long>) connection ->
                    connection.bitCount(key.getBytes(StandardCharsets.UTF_8)));
            if (count != null && count > 0) {
                return count.intValue();
            }
        } catch (Exception e) {
            log.warn("签到 Bitmap 统计失败，userId={}：{}", userId, e.getMessage());
        }
        List<UserSignIn> records = signInMapper.listByRange(userId, month.atDay(1),
                month.atEndOfMonth().isAfter(today) ? today : month.atEndOfMonth());
        return records.size();
    }

    /**
     * 优先读取本月 Bitmap，从今天对应的最低位向前计算连续签到天数，失败时回退数据库。
     */
    private int continuousDays(Long userId, LocalDate today) {
        String key = bitmapKey(userId, YearMonth.from(today));
        try {
            List<Long> result = stringRedisTemplate.opsForValue().bitField(
                    key,
                    BitFieldSubCommands.create()
                            .get(BitFieldSubCommands.BitFieldType.unsigned(today.getDayOfMonth()))
                            .valueAt(0));
            if (result != null && !result.isEmpty() && result.get(0) != null && result.get(0) > 0) {
                long bits = result.get(0);
                int days = 0;
                while ((bits & 1) == 1) {
                    days++;
                    bits >>>= 1;
                }
                return days;
            }
        } catch (Exception e) {
            log.warn("连续签到 Bitmap 统计失败，userId={}：{}", userId, e.getMessage());
        }

        LocalDate start = today.minusDays(59);
        List<UserSignIn> records = signInMapper.listByRange(userId, start, today);
        Set<LocalDate> dates = new HashSet<>();
        for (UserSignIn record : records) {
            dates.add(record.getSignDate());
        }
        int days = 0;
        LocalDate cursor = today;
        while (dates.contains(cursor)) {
            days++;
            cursor = cursor.minusDays(1);
        }
        return days;
    }

    /**
     * 签到 Bitmap 的 key：用户 + 年月。
     */
    private String bitmapKey(Long userId, YearMonth month) {
        return "pet:signin:" + userId + ":" + month.format(MONTH_FORMATTER);
    }
}