package com.example.color_walk.service;

import com.example.color_walk.dto.request.GeminiRequest;
import com.example.color_walk.dto.response.ColorAnalysisResponse;
import com.example.color_walk.dto.response.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ColorAnalysisService {

    private final GeminiService geminiService;

    public ColorAnalysisResponse analyzeImageColors(MultipartFile imageFile) {
        try {
            String base64Image = encodeImageToBase64(imageFile);
            String prompt = createColorAnalysisPrompt();
            
            GeminiRequest request = geminiService.buildGeminiRequestWithImage(prompt, base64Image, imageFile.getContentType());
            GeminiResponse response = geminiService.callGeminiApi(request);
            
            return parseColorAnalysisResponse(response);
        } catch (IOException e) {
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }

    private String encodeImageToBase64(MultipartFile imageFile) throws IOException {
        byte[] imageBytes = imageFile.getBytes();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private String createColorAnalysisPrompt() {
        return "이미지를 분석하여 다음 정보를 JSON 형태로 제공해주세요:\n" +
               "1. 주요 색상 5개 (색상명과 HEX 코드)\n" +
               "2. 전체적인 색감 분위기 (따뜻함/차가움)\n" +
               "3. 색상 조화도 점수 (1-10점)\n" +
               "4. 추천 색상 테마\n" +
               "5. 이미지에 대한 간단한 설명\n\n" +
               "응답 형식:\n" +
               "{\n" +
               "  \"dominantColors\": [\n" +
               "    {\"name\": \"색상명\", \"hex\": \"#FFFFFF\", \"percentage\": 25.5}\n" +
               "  ],\n" +
               "  \"mood\": \"warm\" 또는 \"cool\",\n" +
               "  \"harmonyScore\": 8,\n" +
               "  \"recommendedTheme\": \"자연\",\n" +
               "  \"description\": \"이미지 설명\"\n" +
               "}";
    }

    private ColorAnalysisResponse parseColorAnalysisResponse(GeminiResponse response) {
        String responseText = response.getCandidates().get(0).getContent().getParts().get(0).getText();
        
        try {
            return ColorAnalysisResponse.fromJsonString(responseText);
        } catch (Exception e) {
            return ColorAnalysisResponse.createErrorResponse("응답 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}