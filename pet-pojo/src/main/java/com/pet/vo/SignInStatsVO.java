package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 签到统计视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInStatsVO implements Serializable {

    private String month;

    private Boolean signedToday;

    private Integer signedDays;

    private Integer continuousDays;

    private Integer points;

    private Integer todayPoints;
}