package com.example.color_walk.dto.response;

import com.example.color_walk.domain.Photo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {

    private Long id;
    private Long walkId;
    private String s3Url;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PhotoResponse from(Photo photo) {
        return PhotoResponse.builder()
                .id(photo.getId())
                .walkId(photo.getWalk().getId())
                .s3Url(photo.getS3Url())
                .latitude(photo.getLatitude())
                .longitude(photo.getLongitude())
                .description(photo.getDescription())
                .createdAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .build();
    }
}
