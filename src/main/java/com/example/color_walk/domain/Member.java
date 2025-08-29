package com.example.color_walk.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String nickname;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Walk> walks = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCharacter> ownedCharacters = new ArrayList<>();

    @Builder.Default
    private Long point = 0L;

    // 획득한 컬러들
    @ElementCollection(targetClass = Color.class)
    @CollectionTable(
            name = "member_colors",
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Column(name = "color")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Color> acquiredColors = new HashSet<>();

    // 컬러 획득 메서드
    public void acquireColor(Color color) {
        this.acquiredColors.add(color);
    }

    // 컬러 획득 여부 확인
    public boolean hasAcquiredColor(Color color) {
        return this.acquiredColors.contains(color);
    }

    // 획득한 컬러 개수
    public int getAcquiredColorCount() {
        return this.acquiredColors.size();
    }

    // 포인트 추가
    public void addPoints(Long amount) {
        this.point += amount;
    }

    // 포인트 차감 (성공 시 true, 실패 시 false 반환)
    public boolean deductPoints(Long amount) {
        if (this.point >= amount) {
            this.point -= amount;
            return true;
        }
        return false;
    }

    // 포인트 충분한지 확인
    public boolean hasEnoughPoints(Long amount) {
        return this.point >= amount;
    }

    // 캐릭터 소유 여부 확인
    public boolean hasCharacter(Long characterId) {
        return ownedCharacters.stream()
                .anyMatch(mc -> mc.getCharacter().getId().equals(characterId));
    }

    // 특정 색상 캐릭터 개수 확인
    public long getCharacterCountByColor(Color color) {
        return ownedCharacters.stream()
                .filter(mc -> mc.getCharacter().getColor() == color)
                .mapToInt(MemberCharacter::getQuantity)
                .sum();
    }
}