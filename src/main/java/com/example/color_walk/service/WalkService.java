package com.example.color_walk.service;

import com.example.color_walk.domain.Member;
import com.example.color_walk.domain.Places;
import com.example.color_walk.domain.Walk;
import com.example.color_walk.domain.WalkingPoint;
import com.example.color_walk.dto.request.PlaceRecommendationRequest;
import com.example.color_walk.dto.request.StartWalkRequest;
import com.example.color_walk.dto.request.WalkingPointRequest;
import com.example.color_walk.dto.response.PlaceRecommendationResponse;
import com.example.color_walk.dto.response.WalkResponse;
import com.example.color_walk.repository.MemberRepository;
import com.example.color_walk.repository.PlacesRepository;
import com.example.color_walk.repository.WalkRepository;
import com.example.color_walk.repository.WalkingPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.color_walk.dto.response.WalkResponse.convertToWalkResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class WalkService {

    private final WalkRepository walkRepository;
    private final WalkingPointRepository walkingPointRepository;
    private final MemberRepository memberRepository;
    private final PlacesRepository placesRepository;
    private final GeminiService geminiService;

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

        return WalkResponse.builder()
                .walkId(savedWalk.getId())
                .memberId(savedWalk.getMember().getId())
                .title(savedWalk.getTitle())
                .content(savedWalk.getContent())
                .startTime(savedWalk.getStartTime())
                .colorTheme(savedWalk.getColorTheme())
                .totalDistance(savedWalk.getTotalDistance())
                .build();
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
        Walk walk = walkRepository.findById(walkId)
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
     * 모든 산책 목록 조회 (최신 업데이트 순)
     */
    @Transactional(readOnly = true)
    public List<WalkResponse> getAllWalks() {
        List<Walk> walks = walkRepository.findAllByOrderByUpdatedAtDesc();
        
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

    @Transactional(readOnly = true)
    public PlaceRecommendationResponse recommendPlaces(PlaceRecommendationRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        List<Places> candidatePlaces = getCandidatePlaces(request);

        if (candidatePlaces.isEmpty()) {
            return PlaceRecommendationResponse.builder()
                    .recommendationReason("추천할 수 있는 장소가 없습니다. 검색 조건을 완화해보세요.")
                    .recommendedPlaces(new ArrayList<>())
                    .build();
        }

        List<PlaceRecommendationResponse.RecommendedPlace> recommendedPlaces = 
                getAIRecommendedPlaces(candidatePlaces, request);

        String overallReason = generateOverallRecommendationReason(request, recommendedPlaces);

        return PlaceRecommendationResponse.builder()
                .recommendationReason(overallReason)
                .recommendedPlaces(recommendedPlaces)
                .build();
    }

    private List<Places> getCandidatePlaces(PlaceRecommendationRequest request) {
        // 카테고리 필터링 제거 - 모든 장소를 후보로 사용
        List<Places> candidatePlaces = placesRepository.findAll();

        // 거리 필터링은 유지
        if (request.getCurrentLatitude() != null && request.getCurrentLongitude() != null 
                && request.getMaxDistance() != null) {
            candidatePlaces = candidatePlaces.stream()
                    .filter(place -> {
                        double distance = calculateDistance(
                                request.getCurrentLatitude().doubleValue(),
                                request.getCurrentLongitude().doubleValue(),
                                place.getLatitude().doubleValue(),
                                place.getLongitude().doubleValue()
                        );
                        return distance <= request.getMaxDistance() * 1000; // km to meters
                    })
                    .collect(Collectors.toList());
        }

        return candidatePlaces;
    }

    private List<PlaceRecommendationResponse.RecommendedPlace> getAIRecommendedPlaces(
            List<Places> candidatePlaces, PlaceRecommendationRequest request) {
        
        String aiPrompt = buildAIPrompt(candidatePlaces, request);
        
        try {
            String aiResponse = callGeminiForRecommendation(aiPrompt);
            return parseAIResponseAndSelectPlaces(candidatePlaces, aiResponse, request);
        } catch (Exception e) {
            return getFallbackRecommendations(candidatePlaces, request);
        }
    }

    private String buildAIPrompt(List<Places> places, PlaceRecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("**중요**: 반드시 아래 제공된 후보 장소들 중에서만 선택해주세요. 목록에 없는 장소는 절대 추천하지 마세요.\n\n");
        
        prompt.append("사용자 선호도:\n");
        if (request.getPreferredColorTheme() != null) {
            prompt.append("- 선호 색상 테마: ").append(request.getPreferredColorTheme()).append("\n");
        }
        if (request.getPreferredCategories() != null && !request.getPreferredCategories().isEmpty()) {
            prompt.append("- 선호 카테고리: ").append(request.getPreferredCategories()).append("\n");
        }
        if (request.getActivityLevel() != null) {
            prompt.append("- 활동 수준: ").append(request.getActivityLevel()).append("\n");
        }
        if (request.getWeatherCondition() != null) {
            prompt.append("- 날씨 상태: ").append(request.getWeatherCondition()).append("\n");
        }
        if (request.getTimeOfDay() != null) {
            prompt.append("- 시간대: ").append(request.getTimeOfDay()).append("\n");
        }
        if (request.getAdditionalRequirements() != null) {
            prompt.append("- 추가 요구사항: ").append(request.getAdditionalRequirements()).append("\n");
        }
        if (request.getCurrentLatitude() != null && request.getCurrentLongitude() != null) {
            prompt.append("- 현재 위치: 위도 ").append(request.getCurrentLatitude())
                    .append(", 경도 ").append(request.getCurrentLongitude()).append("\n");
        }
        if (request.getMaxDistance() != null) {
            prompt.append("- 최대 이동 거리: ").append(request.getMaxDistance()).append("km\n");
        }
        
        prompt.append("\n**후보 장소 목록** (이 목록에서만 선택하세요):\n");
        for (int i = 0; i < places.size() && i < 20; i++) {
            Places place = places.get(i);
            prompt.append(String.format("%d. %s (%s)\n   주소: %s\n   설명: %s\n\n", 
                    i + 1, place.getName(), place.getCategory(), 
                    place.getAddress() != null ? place.getAddress() : "주소 없음",
                    place.getDescription() != null ? place.getDescription() : "설명 없음"));
        }
        
        prompt.append("\n**응답 형식** (정확히 이 형식으로만 답변하세요):\n");
        prompt.append("1. [1] [위 목록의 정확한 장소명] - [추천이유]\n");
        prompt.append("2. [2] [위 목록의 정확한 장소명] - [추천이유]\n");
        prompt.append("3. [3] [위 목록의 정확한 장소명] - [추천이유]\n");
        prompt.append("4. [4] [위 목록의 정확한 장소명] - [추천이유]\n");
        prompt.append("5. [5] [위 목록의 정확한 장소명] - [추천이유]\n");
        
        prompt.append("\n**필수 준수사항**:\n");
        prompt.append("1. 위 후보 목록에 있는 장소만 선택\n");
        prompt.append("2. 장소명은 목록에 있는 이름과 정확히 일치시킬 것\n");
        prompt.append("3. 서로 다른 5개 장소 선택 (중복 금지)\n");
        prompt.append("4. 번호는 [1], [2], [3], [4], [5]로 표시\n");
        prompt.append("5. 다른 설명이나 추가 텍스트 없이 위 형식만 사용\n");
        
        return prompt.toString();
    }

    private String callGeminiForRecommendation(String prompt) {
        try {
            var geminiRequest = geminiService.buildGeminiRequestWithImage(prompt, "", "text/plain");
            var geminiResponse = geminiService.callGeminiApi(geminiRequest);
            
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()) {
                var candidate = geminiResponse.getCandidates().get(0);
                if (candidate.getContent() != null && candidate.getContent().getParts() != null 
                        && !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }
            
            throw new RuntimeException("Gemini API 응답이 비어있습니다.");
        } catch (Exception e) {
            throw new RuntimeException("AI 추천 서비스 호출 중 오류가 발생했습니다.", e);
        }
    }

    private List<PlaceRecommendationResponse.RecommendedPlace> parseAIResponseAndSelectPlaces(
            List<Places> candidatePlaces, String aiResponse, PlaceRecommendationRequest request) {
        
        List<PlaceRecommendationResponse.RecommendedPlace> result = new ArrayList<>();
        List<Long> selectedPlaceIds = new ArrayList<>(); // 중복 방지를 위한 ID 추적
        String[] lines = aiResponse.split("\n");
        
        // AI 응답을 로그로 출력 (디버깅용)
        System.out.println("AI Response: " + aiResponse);
        System.out.println("Candidate places count: " + candidatePlaces.size());
        for (int i = 0; i < candidatePlaces.size(); i++) {
            System.out.println((i+1) + ". " + candidatePlaces.get(i).getName() + " (ID: " + candidatePlaces.get(i).getId() + ")");
        }
        
        for (String line : lines) {
            if (line.trim().matches("\\d+\\.\\s*\\[\\d+\\].*")) {
                System.out.println("Processing line: " + line);
                String[] parts = line.split("-", 2);
                if (parts.length >= 2) {
                    String placePart = parts[0].trim();
                    String reason = parts[1].trim();
                    
                    // [번호] 추출 - 예: "1. [3] 장소명" -> "3"
                    String extractedNumber = "";
                    if (placePart.matches(".*\\[\\d+\\].*")) {
                        extractedNumber = placePart.replaceAll(".*\\[(\\d+)\\].*", "$1");
                        System.out.println("Extracted number: " + extractedNumber);
                    }
                    
                    Places matchedPlace = null;
                    
                    // 1. [번호]로 직접 매칭
                    if (!extractedNumber.isEmpty()) {
                        try {
                            int placeIndex = Integer.parseInt(extractedNumber) - 1;
                            if (placeIndex >= 0 && placeIndex < candidatePlaces.size()) {
                                Places candidatePlace = candidatePlaces.get(placeIndex);
                                if (!selectedPlaceIds.contains(candidatePlace.getId())) {
                                    matchedPlace = candidatePlace;
                                    System.out.println("Matched by number: " + matchedPlace.getName());
                                } else {
                                    System.out.println("Place already selected (duplicate): " + candidatePlace.getName());
                                }
                            } else {
                                System.out.println("Place index out of bounds: " + placeIndex + " (candidates: " + candidatePlaces.size() + ")");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Number parsing failed: " + extractedNumber);
                        }
                    }
                    
                    // 2. [번호] 뒤의 장소명으로 매칭 시도
                    if (matchedPlace == null) {
                        // "1. [3] 장소명" -> "장소명" 추출
                        String placeName = placePart.replaceAll("\\d+\\.\\s*\\[\\d+\\]\\s*", "").trim();
                        System.out.println("Trying to match by name: " + placeName);
                        
                        final String finalPlaceName = placeName;
                        matchedPlace = candidatePlaces.stream()
                                .filter(p -> !selectedPlaceIds.contains(p.getId())) // 중복 체크 먼저
                                .filter(p -> p.getName().equals(finalPlaceName) || 
                                           p.getName().contains(finalPlaceName) || 
                                           finalPlaceName.contains(p.getName()))
                                .findFirst()
                                .orElse(null);
                        
                        if (matchedPlace != null) {
                            System.out.println("Matched by name: " + matchedPlace.getName());
                        }
                    }
                    
                    if (matchedPlace != null) {
                        double distanceFromUser = 0.0;
                        if (request.getCurrentLatitude() != null && request.getCurrentLongitude() != null) {
                            distanceFromUser = calculateDistance(
                                    request.getCurrentLatitude().doubleValue(),
                                    request.getCurrentLongitude().doubleValue(),
                                    matchedPlace.getLatitude().doubleValue(),
                                    matchedPlace.getLongitude().doubleValue()
                            ) / 1000.0; // Convert to km
                        }
                        
                        result.add(PlaceRecommendationResponse.RecommendedPlace.builder()
                                .placeId(matchedPlace.getId())
                                .name(matchedPlace.getName())
                                .description(matchedPlace.getDescription())
                                .latitude(matchedPlace.getLatitude().doubleValue())
                                .longitude(matchedPlace.getLongitude().doubleValue())
                                .address(matchedPlace.getAddress())
                                .imageUrl(matchedPlace.getImageUrl())
                                .category(matchedPlace.getCategory().toString())
                                .distanceFromUser(distanceFromUser)
                                .aiRecommendationReason(reason)
                                .priority(result.size() + 1)
                                .build());
                        
                        selectedPlaceIds.add(matchedPlace.getId()); // 선택된 장소 ID 추가
                        System.out.println("Added place: " + matchedPlace.getName() + " (ID: " + matchedPlace.getId() + ")");
                        
                        if (result.size() >= 5) {
                            break;
                        }
                    } else {
                        System.out.println("Could not match place for line: " + line);
                    }
                } else {
                    System.out.println("Could not split line with '-': " + line);
                }
            } else {
                System.out.println("Line doesn't match pattern: " + line);
            }
        }
        
        System.out.println("AI matched places count: " + result.size());
        
        // 부족한 경우 폴백으로 채우기
        if (result.size() < 5) {
            System.out.println("Adding fallback recommendations...");
            for (Places place : candidatePlaces) {
                if (!selectedPlaceIds.contains(place.getId()) && result.size() < 5) {
                    double distanceFromUser = 0.0;
                    if (request.getCurrentLatitude() != null && request.getCurrentLongitude() != null) {
                        distanceFromUser = calculateDistance(
                                request.getCurrentLatitude().doubleValue(),
                                request.getCurrentLongitude().doubleValue(),
                                place.getLatitude().doubleValue(),
                                place.getLongitude().doubleValue()
                        ) / 1000.0;
                    }
                    
                    result.add(PlaceRecommendationResponse.RecommendedPlace.builder()
                            .placeId(place.getId())
                            .name(place.getName())
                            .description(place.getDescription())
                            .latitude(place.getLatitude().doubleValue())
                            .longitude(place.getLongitude().doubleValue())
                            .address(place.getAddress())
                            .imageUrl(place.getImageUrl())
                            .category(place.getCategory().toString())
                            .distanceFromUser(distanceFromUser)
                            .aiRecommendationReason("시스템이 선별한 추천 장소입니다.")
                            .priority(result.size() + 1)
                            .build());
                    
                    selectedPlaceIds.add(place.getId());
                    System.out.println("Added fallback place: " + place.getName() + " (ID: " + place.getId() + ")");
                }
            }
        }
        
        System.out.println("Final result count: " + result.size());
        return result;
    }

    private List<PlaceRecommendationResponse.RecommendedPlace> getFallbackRecommendations(
            List<Places> candidatePlaces, PlaceRecommendationRequest request) {
        
        List<PlaceRecommendationResponse.RecommendedPlace> fallbackPlaces = candidatePlaces.stream()
                .limit(5)
                .map(place -> {
                    double distanceFromUser = 0.0;
                    if (request.getCurrentLatitude() != null && request.getCurrentLongitude() != null) {
                        distanceFromUser = calculateDistance(
                                request.getCurrentLatitude().doubleValue(),
                                request.getCurrentLongitude().doubleValue(),
                                place.getLatitude().doubleValue(),
                                place.getLongitude().doubleValue()
                        ) / 1000.0;
                    }
                    
                    return PlaceRecommendationResponse.RecommendedPlace.builder()
                            .placeId(place.getId())
                            .name(place.getName())
                            .description(place.getDescription())
                            .latitude(place.getLatitude().doubleValue())
                            .longitude(place.getLongitude().doubleValue())
                            .address(place.getAddress())
                            .imageUrl(place.getImageUrl())
                            .category(place.getCategory().toString())
                            .distanceFromUser(distanceFromUser)
                            .aiRecommendationReason("시스템이 선별한 추천 장소입니다.")
                            .priority(candidatePlaces.indexOf(place) + 1)
                            .build();
                })
                .collect(Collectors.toList());
        
        return fallbackPlaces;
    }

    private String generateOverallRecommendationReason(PlaceRecommendationRequest request, 
            List<PlaceRecommendationResponse.RecommendedPlace> places) {
        StringBuilder reason = new StringBuilder();
        reason.append("사용자의 ");
        
        if (request.getPreferredColorTheme() != null) {
            reason.append(request.getPreferredColorTheme()).append(" 색상 테마 선호도, ");
        }
        if (request.getActivityLevel() != null) {
            reason.append(request.getActivityLevel()).append(" 활동 수준, ");
        }
        if (request.getPreferredCategories() != null && !request.getPreferredCategories().isEmpty()) {
            reason.append(request.getPreferredCategories().toString()).append(" 카테고리 선호도");
        }
        
        reason.append("를 고려하여 AI가 선정한 맞춤형 산책로 추천입니다.");
        
        return reason.toString();
    }
}
