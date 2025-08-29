package com.example.color_walk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColorMatchingResult {
    
    /**
     * 사용자가 선택한 색상 테마
     */
    private String selectedColorTheme;
    
    /**
     * 분석된 주요 색상들
     */
    private List<ColorAnalysisResponse.DominantColor> analyzedColors;
    
    /**
     * 전체 매칭 점수 (0-100)
     */
    private Double overallMatchingScore;
    
    /**
     * 매칭 추천 메시지
     */
    private String recommendation;
    
    /**
     * 상세 색상 분석 결과
     */
    private ColorAnalysisResponse analysisResponse;
    
    /**
     * 총 분석된 사진 수
     */
    private Integer totalPhotos;
    
    /**
     * 분석 성공 여부
     */
    private Boolean success;
    
    /**
     * 오류 메시지 (분석 실패 시)
     */
    private String errorMessage;
    
    /**
     * 성공적인 색상 매칭 결과 생성
     */
    public static ColorMatchingResult createSuccessResult(String selectedColorTheme, 
                                                         ColorAnalysisResponse analysisResponse,
                                                         Double matchingScore, 
                                                         String recommendation,
                                                         Integer totalPhotos) {
        return ColorMatchingResult.builder()
                .selectedColorTheme(selectedColorTheme)
                .analyzedColors(analysisResponse.getDominantColors())
                .overallMatchingScore(matchingScore)
                .recommendation(recommendation)
                .analysisResponse(analysisResponse)
                .totalPhotos(totalPhotos)
                .success(true)
                .build();
    }
    
    /**
     * 실패한 색상 매칭 결과 생성
     */
    public static ColorMatchingResult createErrorResult(String errorMessage) {
        return ColorMatchingResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
}