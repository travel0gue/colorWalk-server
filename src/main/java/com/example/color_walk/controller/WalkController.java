package com.example.color_walk.controller;

import com.example.color_walk.dto.request.StartWalkRequest;
import com.example.color_walk.dto.request.WalkingPointRequest;
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
     * 모든 산책 목록 조회
     */
    @GetMapping("/all")
    @Operation(summary = "모든 산책 목록 조회", description = "모든 산책을 최신 업데이트 순으로 조회")
    public ResponseEntity<List<WalkResponse>> getAllWalks() {
        List<WalkResponse> responses = walkService.getAllWalks();
        return ResponseEntity.ok(responses);
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
}
