package com.example.color_walk.controller;

import com.example.color_walk.domain.Color;
import com.example.color_walk.domain.Member;
import com.example.color_walk.domain.MemberCharacter;
import com.example.color_walk.domain.Character;
import com.example.color_walk.dto.request.GachaRequest;
import com.example.color_walk.dto.response.CharacterResponse;
import com.example.color_walk.repository.MemberRepository;
import com.example.color_walk.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/characters")
@RequiredArgsConstructor
@Tag(name = "Character", description = "캐릭터 관련 API")
public class CharacterController {

    private final CharacterService characterService;
    private final MemberRepository memberRepository;

    /**
     * 캐릭터 뽑기
     */
    @PostMapping("/draw")
    @Operation(summary = "캐릭터 가챠", description = "포인트를 사용하여 색상별 캐릭터를 뽑습니다" +
            "본인이 획득한 색상의 캐릭터만 뽑을 수 있음")
    public ResponseEntity<CharacterResponse> drawCharacter(@RequestBody GachaRequest request) {
        try {
            MemberCharacter result = characterService.drawCharacter(
                    request.getMemberId(),
                    request.getSelectedColor()
            );

            // 뽑기 후 남은 포인트 조회
            Member updatedMember = memberRepository.findById(request.getMemberId()).orElseThrow();

            CharacterResponse response = CharacterResponse.from(result, updatedMember.getPoint());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 멤버가 획득한 색상 목록 조회
     */
    @GetMapping("/colors/{memberId}")
    @Operation(summary = "획득한 색상 목록 조회", description = "멤버가 획득한 색상 목록을 조회합니다. 맴버는 자신이 획득한 색상 중 하나를 선택해서 가챠를 돌릴 수 있음.")
    public ResponseEntity<Set<Color>> getAcquiredColors(@PathVariable("memberId") Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        return ResponseEntity.ok(member.getAcquiredColors());
    }

    /**
     * 멤버가 소유한 캐릭터 목록 조회
     */
    @GetMapping("/my/{memberId}")
    @Operation(summary = "보유 캐릭터 목록 조회", description = "멤버가 보유한 캐릭터 목록을 조회합니다")
    public ResponseEntity<List<CharacterResponse>> getMyCharacters(@PathVariable("memberId") Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        List<CharacterResponse> myCharacters = characterService.getMemberCharacters(memberId, member.getPoint());
        return ResponseEntity.ok(myCharacters);
    }

    /**
     * 캐릭터 생성
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "캐릭터 생성", description = "프론트에서 무시해도 되는 api 입니다.")
    public ResponseEntity<Character> createCharacter(
            @Parameter(description = "캐릭터 이름", required = true) @RequestParam("name") String name,
            @Parameter(description = "캐릭터 색상", required = true) @RequestParam("color") Color color,
            @Parameter(description = "캐릭터 설명") @RequestParam("description") String description,
            @Parameter(description = "캐릭터 이미지 파일", required = true) @RequestParam("image") MultipartFile imageFile) {
        try {
            Character character = characterService.createCharacter(name, color.name(), description, imageFile);
            return ResponseEntity.ok(character);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 포인트 조회
     */
    @GetMapping("/point/{memberId}")
    public ResponseEntity<Long> getMemberPoint(@PathVariable("memberId") Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return ResponseEntity.ok(member.getPoint());
    }
}
