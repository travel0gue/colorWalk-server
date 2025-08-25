package com.example.color_walk.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Places extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 8, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8, nullable = false)
    private BigDecimal longitude;

    private String address;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private PlaceCategory category;

    public enum PlaceCategory {
        PARK, MUSEUM, RESTAURANT, CAFE, LANDMARK, NATURE, CULTURAL, SHOPPING, OTHER
    }
}