package com.pet.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 社区评论请求参数。
 */
@Data
public class CommunityCommentDTO implements Serializable {

    private String content;
}