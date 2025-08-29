package com.example.color_walk.controller;

import com.example.color_walk.dto.request.PlaceRecommendationRequest;
import com.example.color_walk.dto.request.StartWalkRequest;
import com.example.color_walk.dto.request.WalkingPointRequest;
import com.example.color_walk.dto.response.PlaceRecommendationResponse;
import com.example.color_walk.dto.response.WalkResponse;
import com.example.color_walk.service.WalkService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/walks")
@RequiredArgsConstructor
public class WalkController {

    private final WalkService walkService;

    /**
     * 산책 시작
     */
    @PostMapping("/start")
    @Operation(summary = "산책 시작", description = "산책 시작할 때 호출")
    public ResponseEntity<WalkResponse> startWalk(@Valid @RequestBody StartWalkRequest request) {
        WalkResponse response = walkService.startWalk(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GPS 포인트 저장
     */
    @PostMapping("/points")
    @Operation(summary = "산책 중 위치를 받는 api", description = "산책 중에 일정 간격마다 사용자 위치를 받아 산책 경로를 저장")
    public ResponseEntity<Void> saveWalkingPoint(@Valid @RequestBody WalkingPointRequest request) {
        walkService.saveWalkingPoint(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 산책 종료
     */
    @PutMapping("/{walkId}/finish")
    @Operation(summary = "산책 종료", description = "산책 종료할 때 호출")
    public ResponseEntity<WalkResponse> finishWalk(@PathVariable("walkId") Long walkId) {
        WalkResponse response = walkService.finishWalk(walkId);
        return ResponseEntity.ok(response);
    }

    /**
     * 산책 상세 조회
     */
    @GetMapping("/{walkId}")
    @Operation(summary = "산책 상세 조회")
    public ResponseEntity<WalkResponse> getWalkDetail(@PathVariable("walkId") Long walkId) {
        WalkResponse response = walkService.getWalkDetail(walkId);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원의 산책 목록 조회
     */
    @GetMapping("/members/{memberId}")
    @Operation(summary = "산책 목록 조회")
    public ResponseEntity<List<WalkResponse>> getMemberWalks(@PathVariable("memberId") Long memberId) {
        List<WalkResponse> responses = walkService.getMemberWalks(memberId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/recommend")
    @Operation(summary = "AI 기반 장소 추천", 
               description = "사용자의 취향과 선호도를 바탕으로 AI가 추천하는 산책로 장소 5곳을 제공합니다")
    public ResponseEntity<PlaceRecommendationResponse> recommendPlaces(
            @Valid @RequestBody PlaceRecommendationRequest request) {
        PlaceRecommendationResponse response = walkService.recommendPlaces(request);
        return ResponseEntity.ok(response);
    }
}
