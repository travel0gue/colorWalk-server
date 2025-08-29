package com.example.color_walk.dto.request;

import com.example.color_walk.domain.Places.PlaceCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PlaceRecommendationRequest {

    @NotNull(message = "회원 ID는 필수입니다")
    private Long memberId;

    private String preferredColorTheme;

    @Size(max = 3, message = "선호 카테고리는 최대 3개까지 선택 가능합니다")
    private List<PlaceCategory> preferredCategories;

    private BigDecimal currentLatitude;

    private BigDecimal currentLongitude;

    @Size(max = 50, message = "최대 이동 거리는 50km 이내여야 합니다")
    private Double maxDistance;

    private String activityLevel;

    @Size(max = 500, message = "추가 요구사항은 500자 이내로 작성해주세요")
    private String additionalRequirements;

    private String weatherCondition;

    private String timeOfDay;
}