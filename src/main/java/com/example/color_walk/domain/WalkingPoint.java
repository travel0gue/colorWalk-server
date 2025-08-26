package com.example.color_walk.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "walking_point")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalkingPoint extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id", nullable = false)
    private Walk walk;

    private Double latitude;   // 위도
    private Double longitude;  // 경도
    private LocalDateTime timestamp;
    private Integer sequence;  // 순서
}
