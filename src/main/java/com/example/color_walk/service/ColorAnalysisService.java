package com.example.color_walk.service;

import com.example.color_walk.dto.request.GeminiRequest;
import com.example.color_walk.dto.response.ColorAnalysisResponse;
import com.example.color_walk.dto.response.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

    /**
     * 여러 이미지 파일의 종합적인 색상 분석을 수행
     */
    public ColorAnalysisResponse analyzeMultipleImageColors(List<MultipartFile> imageFiles) {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return ColorAnalysisResponse.createErrorResponse("분석할 이미지가 없습니다.");
        }
        
        try {
            // 여러 이미지를 base64로 인코딩하고 종합 분석 프롬프트 생성
            List<String> base64Images = new ArrayList<>();
            List<String> mimeTypes = new ArrayList<>();
            List<String> imageNames = new ArrayList<>();
            
            for (MultipartFile imageFile : imageFiles) {
                base64Images.add(encodeImageToBase64(imageFile));
                mimeTypes.add(imageFile.getContentType());
                imageNames.add(imageFile.getOriginalFilename());
            }
            
            String prompt = createMultipleImagesAnalysisPrompt(imageNames.size());
            
            // 모든 이미지를 동시에 Gemini에 전달하여 종합 분석
            GeminiRequest request = geminiService.buildGeminiRequestWithMultipleImages(prompt, base64Images, mimeTypes);
            GeminiResponse response = geminiService.callGeminiApi(request);
            
            ColorAnalysisResponse result = parseColorAnalysisResponse(response);
            result.setDescription("총 " + imageFiles.size() + "장의 이미지를 실시간 종합 분석한 결과입니다. " + result.getDescription());
            
            return result;
        } catch (IOException e) {
            return ColorAnalysisResponse.createErrorResponse("이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            return ColorAnalysisResponse.createErrorResponse("여러 이미지 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private String createMultipleImagesAnalysisPrompt(int imageCount) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("**중요: 반드시 JSON 형식으로만 응답해주세요. 다른 설명이나 텍스트 없이 오직 JSON만 반환하세요.**\n\n");
        
        prompt.append("제공된 총 ").append(imageCount).append("장의 이미지들을 분석해주세요:\n");
        prompt.append("1. 각 이미지별로 주요 색상 3개씩 간략하게 분석\n");
        prompt.append("2. 전체 이미지들의 종합적인 색상 테마 분석\n\n");
        
        prompt.append("**다음 JSON 형식으로만 응답하세요: 색깔은 빨강,파랑,노랑,초록,보라,주황,분홍,검정,하양,갈색 중에 하나로 해주세요**\n");
        prompt.append("{\n");
        prompt.append("  \"dominantColors\": [\n");
        prompt.append("    {\"name\": \"초록\", \"hex\": \"#32CD32\", \"percentage\": 35.5},\n");
        prompt.append("    {\"name\": \"파랑\", \"hex\": \"#87CEEB\", \"percentage\": 25.0},\n");
        prompt.append("    {\"name\": \"갈색\", \"hex\": \"#8B4513\", \"percentage\": 20.0},\n");
        prompt.append("    {\"name\": \"하양\", \"hex\": \"#FFFFFF\", \"percentage\": 15.0},\n");
        prompt.append("    {\"name\": \"검정\", \"hex\": \"#808080\", \"percentage\": 4.5}\n");
        prompt.append("  ],\n");
        prompt.append("  \"mood\": \"cool\",\n");
        prompt.append("  \"harmonyScore\": 8,\n");
        prompt.append("  \"recommendedTheme\": \"자연\",\n");
        prompt.append("  \"description\": \"").append(imageCount).append("장의 이미지가 조화롭게 어우러진 종합 분석 결과입니다.\",\n");
        prompt.append("  \"individualImages\": [\n");
        for (int i = 1; i <= imageCount; i++) {
            prompt.append("    {\n");
            prompt.append("      \"imageIndex\": ").append(i).append(",\n");
            prompt.append("      \"imageName\": \"image_").append(i).append("\",\n");
            prompt.append("      \"dominantColors\": [\n");
            prompt.append("        {\"name\": \"색상1\", \"hex\": \"#FFFFFF\", \"percentage\": 40.0},\n");
            prompt.append("        {\"name\": \"색상2\", \"hex\": \"#000000\", \"percentage\": 35.0},\n");
            prompt.append("        {\"name\": \"색상3\", \"hex\": \"#FF0000\", \"percentage\": 25.0}\n");
            prompt.append("      ]\n");
            prompt.append("    }");
            if (i < imageCount) prompt.append(",");
            prompt.append("\n");
        }
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        
        prompt.append("**규칙:**\n");
        prompt.append("1. mood는 \"warm\" 또는 \"cool\" 중 하나만 사용\n");
        prompt.append("2. harmonyScore는 1-10 사이의 정수\n");
        prompt.append("3. dominantColors는 정확히 5개 색상\n");
        prompt.append("4. percentage의 합계는 100이 되어야 함\n");
        prompt.append("5. hex 코드는 #으로 시작\n");
        prompt.append("6. JSON 외의 다른 텍스트는 절대 포함하지 마세요\n");
        prompt.append("7. 설명이나 추가 코멘트 금지\n");
        
        return prompt.toString();
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