package com.example.color_walk.controller;

import com.example.color_walk.dto.request.StartWalkRequest;
import com.example.color_walk.dto.request.WalkingPointRequest;
import com.example.color_walk.dto.response.WalkResponse;
import com.example.color_walk.service.WalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/walks")
@RequiredArgsConstructor
public class WalkController {

    private final WalkService walkService;

    /**
     * 산책 시작
     */
    @PostMapping("/start")
    public ResponseEntity<WalkResponse> startWalk(@Valid @RequestBody StartWalkRequest request) {
        WalkResponse response = walkService.startWalk(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GPS 포인트 저장
     */
    @PostMapping("/points")
    public ResponseEntity<Void> saveWalkingPoint(@Valid @RequestBody WalkingPointRequest request) {
        walkService.saveWalkingPoint(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 산책 종료
     */
    @PutMapping("/{walkId}/finish")
    public ResponseEntity<WalkResponse> finishWalk(@PathVariable("walkId") Long walkId) {
        WalkResponse response = walkService.finishWalk(walkId);
        return ResponseEntity.ok(response);
    }

    /**
     * 산책 상세 조회
     */
    @GetMapping("/{walkId}")
    public ResponseEntity<WalkResponse> getWalkDetail(@PathVariable("walkId") Long walkId) {
        WalkResponse response = walkService.getWalkDetail(walkId);
        return ResponseEntity.ok(response);
    }

    /**
     * 회원의 산책 목록 조회
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<WalkResponse>> getMemberWalks(@PathVariable("memberId") Long memberId) {
        List<WalkResponse> responses = walkService.getMemberWalks(memberId);
        return ResponseEntity.ok(responses);
    }
}
