package com.team109.javara.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImageResponse {
    private Long imageId;
    private String imageUrl; // 프론트에서 사용할 URL
}