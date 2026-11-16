package com.pet.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区评论视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityCommentVO implements Serializable {

    private Long id;

    private Long userId;

    private String userName;

    private String content;

    private LocalDateTime createTime;
}