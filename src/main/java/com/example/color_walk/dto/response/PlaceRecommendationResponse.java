package com.example.color_walk.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlaceRecommendationResponse {

    private String recommendationReason;
    
    private List<RecommendedPlace> recommendedPlaces;

    @Data
    @Builder
    public static class RecommendedPlace {
        private Long placeId;
        private String name;
        private String description;
        private Double latitude;
        private Double longitude;
        private String address;
        private String imageUrl;
        private String category;
        private Double distanceFromUser;
        private String aiRecommendationReason;
        private Integer priority;
    }
}