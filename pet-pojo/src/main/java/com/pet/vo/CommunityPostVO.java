package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 社区动态视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityPostVO implements Serializable {

    private Long id;

    private Long userId;

    private String userName;

    private String userAvatar;

    private Long petId;

    private String petName;

    private String petSpecies;

    private Long storeId;

    private String storeName;

    private String content;

    private String imagesJson;

    private List<String> images;

    private Integer likeCount;

    private Integer commentCount;

    private Boolean liked;

    private Integer status;

    private LocalDateTime createTime;
}