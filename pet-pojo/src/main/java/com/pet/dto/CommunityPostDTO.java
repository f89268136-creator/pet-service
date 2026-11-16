package com.pet.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 社区动态发布参数。
 */
@Data
public class CommunityPostDTO implements Serializable {

    private String content;

    private Long petId;

    private Long storeId;

    private List<String> images;
}