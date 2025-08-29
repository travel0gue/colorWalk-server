package com.example.color_walk.dto.request;

import com.example.color_walk.domain.Places.PlaceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlaceCreateRequest {

    @NotBlank(message = "장소명은 필수입니다")
    private String name;

    private String description;

    @NotNull(message = "위도는 필수입니다")
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수입니다")
    private BigDecimal longitude;

    private String address;

    private String imageUrl;

    @NotNull(message = "카테고리는 필수입니다")
    private PlaceCategory category;
}