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
            List<String> imageNames = new ArrayList<>();
            
            for (MultipartFile imageFile : imageFiles) {
                base64Images.add(encodeImageToBase64(imageFile));
                imageNames.add(imageFile.getOriginalFilename());
            }
            
            String prompt = createMultipleImagesAnalysisPrompt(imageNames.size());
            
            // 첫 번째 이미지를 메인으로 사용하여 Gemini에 요청
            // TODO: 실제로는 모든 이미지를 동시에 분석할 수 있도록 Gemini API 확장 필요
            GeminiRequest request = geminiService.buildGeminiRequestWithImage(prompt, base64Images.get(0), imageFiles.get(0).getContentType());
            GeminiResponse response = geminiService.callGeminiApi(request);
            
            ColorAnalysisResponse result = parseColorAnalysisResponse(response);
            result.setDescription("총 " + imageFiles.size() + "장의 이미지를 종합 분석한 결과입니다. " + result.getDescription());
            
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
        
        prompt.append("총 ").append(imageCount).append("장의 이미지를 종합적으로 분석하여 전체적인 색상 테마를 파악해주세요.\n\n");
        
        prompt.append("**다음 JSON 형식으로만 응답하세요:**\n");
        prompt.append("{\n");
        prompt.append("  \"dominantColors\": [\n");
        prompt.append("    {\"name\": \"초록색\", \"hex\": \"#32CD32\", \"percentage\": 35.5},\n");
        prompt.append("    {\"name\": \"파란색\", \"hex\": \"#87CEEB\", \"percentage\": 25.0},\n");
        prompt.append("    {\"name\": \"갈색\", \"hex\": \"#8B4513\", \"percentage\": 20.0},\n");
        prompt.append("    {\"name\": \"흰색\", \"hex\": \"#FFFFFF\", \"percentage\": 15.0},\n");
        prompt.append("    {\"name\": \"회색\", \"hex\": \"#808080\", \"percentage\": 4.5}\n");
        prompt.append("  ],\n");
        prompt.append("  \"mood\": \"cool\",\n");
        prompt.append("  \"harmonyScore\": 8,\n");
        prompt.append("  \"recommendedTheme\": \"자연\",\n");
        prompt.append("  \"description\": \"").append(imageCount).append("장의 이미지가 조화롭게 어우러진 종합 분석 결과입니다.\"\n");
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