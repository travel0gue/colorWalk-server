package com.example.color_walk.service;

import com.example.color_walk.domain.Member;
import com.example.color_walk.domain.Walk;
import com.example.color_walk.domain.WalkingPoint;
import com.example.color_walk.dto.request.StartWalkRequest;
import com.example.color_walk.dto.request.WalkingPointRequest;
import com.example.color_walk.dto.response.WalkResponse;
import com.example.color_walk.repository.MemberRepository;
import com.example.color_walk.repository.WalkRepository;
import com.example.color_walk.repository.WalkingPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.color_walk.dto.response.WalkResponse.convertToWalkResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class WalkService {

    private final WalkRepository walkRepository;
    private final WalkingPointRepository walkingPointRepository;
    private final MemberRepository memberRepository;

    /**
     * 산책 시작
     */
    public WalkResponse startWalk(StartWalkRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Walk walk = Walk.builder()
                .member(member)
                .title(request.getTitle())
                .content(request.getContent())
                .colorTheme(request.getColorTheme())
                .startTime(LocalDateTime.now())
                .totalDistance(0.0)
                .build();

        Walk savedWalk = walkRepository.save(walk);

        return convertToWalkResponse(savedWalk);
    }

    /**
     * GPS 포인트 저장
     */
    public void saveWalkingPoint(WalkingPointRequest request) {
        Walk walk = walkRepository.findById(request.getWalkId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 산책입니다."));

        // 현재 산책의 포인트 수를 조회해서 sequence 설정
        int currentPointCount = walkingPointRepository.countByWalkId(request.getWalkId());

        WalkingPoint walkingPoint = WalkingPoint.builder()
                .walk(walk)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : LocalDateTime.now())
                .sequence(currentPointCount + 1)
                .build();

        walkingPointRepository.save(walkingPoint);

        // 총 거리 업데이트 (2개 이상의 포인트가 있을 때)
        if (currentPointCount > 0) {
            updateTotalDistance(walk.getId());
        }
    }

    /**
     * 산책 종료
     */
    public WalkResponse finishWalk(Long walkId) {
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 산책입니다."));

        walk.setEndTime(LocalDateTime.now());
        updateTotalDistance(walkId); // 최종 거리 계산

        Walk updatedWalk = walkRepository.save(walk);

        return convertToWalkResponse(updatedWalk);
    }

    /**
     * 산책 상세 조회
     */
    @Transactional(readOnly = true)
    public WalkResponse getWalkDetail(Long walkId) {
        Walk walk = walkRepository.findByIdWithPoints(walkId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 산책입니다."));

        return convertToWalkResponse(walk);
    }

    /**
     * 회원의 산책 목록 조회
     */
    @Transactional(readOnly = true)
    public List<WalkResponse> getMemberWalks(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<Walk> walks = walkRepository.findByMemberIdOrderByStartTimeDesc(memberId);

        return walks.stream()
                .map(WalkResponse::convertToWalkResponse)
                .toList();
    }

    /**
     * 총 거리 계산 및 업데이트
     */
    private void updateTotalDistance(Long walkId) {
        List<WalkingPoint> points = walkingPointRepository.findByWalkIdOrderBySequence(walkId);

        double totalDistance = 0.0;
        for (int i = 1; i < points.size(); i++) {
            WalkingPoint prev = points.get(i - 1);
            WalkingPoint curr = points.get(i);
            totalDistance += calculateDistance(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude()
            );
        }

        walkRepository.updateTotalDistance(walkId, totalDistance);
    }

    /**
     * 두 GPS 좌표 간 거리 계산 (Haversine 공식)
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // 지구 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c * 1000; // 미터 단위로 반환
    }
}
