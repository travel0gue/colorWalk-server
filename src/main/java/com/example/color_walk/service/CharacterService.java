package com.example.color_walk.service;

import com.example.color_walk.domain.Color;
import com.example.color_walk.domain.Member;
import com.example.color_walk.domain.Character;
import com.example.color_walk.domain.MemberCharacter;
import com.example.color_walk.dto.response.CharacterResponse;
import com.example.color_walk.repository.CharacterRepository;
import com.example.color_walk.repository.MemberCharacterRepository;
import com.example.color_walk.repository.MemberRepository;
import com.example.color_walk.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CharacterService {

    private final MemberRepository memberRepository;
    private final CharacterRepository characterRepository;
    private final MemberCharacterRepository memberCharacterRepository;
    private final S3Service s3Service;

    private static final Long GACHA_COST = 50L;

    /**
     * 캐릭터 뽑기
     */
    public MemberCharacter drawCharacter(Long memberId, Color selectedColor) {
        // 1. 멤버 조회 및 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // 2. 포인트 확인
        if (!member.deductPoints(GACHA_COST)) {
            throw new IllegalStateException("포인트가 부족합니다. 현재 포인트: " + member.getPoint());
        }

//        // 3. 선택한 색상을 보유하고 있는지 확인
//        if (!member.hasAcquiredColor(selectedColor)) {
//            throw new IllegalStateException("획득하지 않은 색상입니다: " + selectedColor.getKoreanName());
//        }

        // 4. 해당 색상의 캐릭터들 조회
        List<Character> availableCharacters = characterRepository.findByColor(selectedColor);
        if (availableCharacters.isEmpty()) {
            throw new IllegalStateException("해당 색상의 캐릭터가 없습니다: " + selectedColor.getKoreanName());
        }

        // 5. 단순 랜덤 뽑기
        Character drawnCharacter = drawRandomCharacter(availableCharacters);

        // 6. 캐릭터 소유 처리
        return addCharacterToMember(member, drawnCharacter);
    }

    /**
     * 멤버가 소유한 캐릭터 목록 조회
     */
    public List<CharacterResponse> getMemberCharacters(Long memberId, Long remainingPoints) {
        return memberCharacterRepository.findByMemberId(memberId).stream()
                .map(mc -> CharacterResponse.from(mc, remainingPoints))
                .collect(Collectors.toList());
    }

    private Character drawRandomCharacter(List<Character> characters) {
        Random random = new Random();
        int randomIndex = random.nextInt(characters.size());
        return characters.get(randomIndex);
    }

    private MemberCharacter addCharacterToMember(Member member, com.example.color_walk.domain.Character character) {
        // 이미 보유한 캐릭터인지 확인
        MemberCharacter existing = memberCharacterRepository
                .findByMemberAndCharacter(member, character)
                .orElse(null);

        if (existing != null) {
            // 중복 획득 시 수량 증가
            existing.setQuantity(existing.getQuantity() + 1);
            return memberCharacterRepository.save(existing);
        } else {
            // 새로운 캐릭터 획득
            MemberCharacter newMemberCharacter = MemberCharacter.builder()
                    .member(member)
                    .character(character)
                    .acquiredAt(LocalDateTime.now())
                    .quantity(1)
                    .build();
            return memberCharacterRepository.save(newMemberCharacter);
        }
    }

    /**
     * 캐릭터 생성
     */
    public Character createCharacter(String name, String color, String description, MultipartFile imageFile) throws Exception {
        String imageUrl = s3Service.uploadFile(imageFile, "characters");
        
        Character character = Character.builder()
                .name(name)
                .imageUrl(imageUrl)
                .color(Color.valueOf(color.toUpperCase()))
                .description(description)
                .build();

        return characterRepository.save(character);
    }
}
