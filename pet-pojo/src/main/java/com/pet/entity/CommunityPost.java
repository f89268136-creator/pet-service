package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区动态实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPost implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Long petId;

    private Long storeId;

    private String content;

    private String images;

    private Integer likeCount;

    private Integer commentCount;

    /**
     * 0隐藏 1展示
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}