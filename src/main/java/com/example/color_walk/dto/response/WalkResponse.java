package com.example.color_walk.dto.response;

import com.example.color_walk.domain.Walk;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class WalkResponse {
    private Long walkId;
    private Long memberId;
    private String title;
    private String content;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double totalDistance;
    private String colorTheme;
    private List<WalkingPointResponse> walkingPoints;
    private List<PhotoResponse> photos;

    /**
     * Walk 엔티티를 WalkResponse로 변환
     */
    public static WalkResponse convertToWalkResponse(Walk walk) {
        List<WalkingPointResponse> walkingPointResponses = walk.getWalkingPoints() != null ? 
                walk.getWalkingPoints().stream()
                        .map(point -> WalkingPointResponse.builder()
                                .pointId(point.getId())
                                .latitude(point.getLatitude())
                                .longitude(point.getLongitude())
                                .timestamp(point.getTimestamp())
                                .sequence(point.getSequence())
                                .build())
                        .toList() : List.of();

        List<PhotoResponse> photoResponses = walk.getPhotos() != null ?
                walk.getPhotos().stream()
                        .map(PhotoResponse::from)
                        .toList() : List.of();

        return WalkResponse.builder()
                .walkId(walk.getId())
                .memberId(walk.getMember().getId())
                .title(walk.getTitle())
                .content(walk.getContent())
                .startTime(walk.getStartTime())
                .endTime(walk.getEndTime())
                .totalDistance(walk.getTotalDistance())
                .colorTheme(walk.getColorTheme().getKoreanName())
                .walkingPoints(walkingPointResponses)
                .photos(photoResponses)
                .build();
    }
}
