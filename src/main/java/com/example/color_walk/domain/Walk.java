package com.example.color_walk.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "walk")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Walk extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double totalDistance;

    @Enumerated(EnumType.STRING)
    @Column(name = "color_theme")
    private Color colorTheme;

    @OneToMany(mappedBy = "walk", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalkingPoint> walkingPoints = new ArrayList<>();

    @OneToMany(mappedBy = "walk", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();
}