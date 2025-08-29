package com.example.color_walk.dto.response;

import com.example.color_walk.domain.Character;
import com.example.color_walk.domain.MemberCharacter;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CharacterResponse {
    private Long characterId;
    private String name;
    private String imageUrl;
    private String color;
    private String colorKoreanName;
    private String description;
    private Integer quantity;
    private LocalDateTime acquiredAt;
    private Long remainingPoints;

    public static CharacterResponse from(MemberCharacter memberCharacter, Long remainingPoints) {
        Character character = memberCharacter.getCharacter();
        return CharacterResponse.builder()
                .characterId(character.getId())
                .name(character.getName())
                .imageUrl(character.getImageUrl())
                .color(character.getColor().name())
                .colorKoreanName(character.getColor().getKoreanName())
                .description(character.getDescription())
                .quantity(memberCharacter.getQuantity())
                .acquiredAt(memberCharacter.getAcquiredAt())
                .remainingPoints(remainingPoints)
                .build();
    }
}
