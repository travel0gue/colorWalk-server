package com.example.color_walk.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Character extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String imageUrl; // S3 URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Color color;

    private String description;
}
