package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户签到记录实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSignIn implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private LocalDate signDate;

    private Integer points;

    private LocalDateTime createTime;
}