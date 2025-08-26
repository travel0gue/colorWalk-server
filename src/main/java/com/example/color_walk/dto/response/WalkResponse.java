package com.example.color_walk.dto.response;

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
}
