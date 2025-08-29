package com.example.color_walk.dto.response;

import com.example.color_walk.domain.Places;
import com.example.color_walk.domain.Places.PlaceCategory;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PlaceResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private String imageUrl;
    private PlaceCategory category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PlaceResponse from(Places place) {
        return PlaceResponse.builder()
                .id(place.getId())
                .name(place.getName())
                .description(place.getDescription())
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .address(place.getAddress())
                .imageUrl(place.getImageUrl())
                .category(place.getCategory())
                .createdAt(place.getCreatedAt())
                .updatedAt(place.getUpdatedAt())
                .build();
    }
}