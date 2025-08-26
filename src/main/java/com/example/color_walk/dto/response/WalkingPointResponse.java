package com.example.color_walk.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WalkingPointResponse {
    private Long pointId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Integer sequence;
}
