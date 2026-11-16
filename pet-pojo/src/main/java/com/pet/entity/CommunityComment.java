package com.pet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 社区评论实体。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityComment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    private LocalDateTime createTime;
}