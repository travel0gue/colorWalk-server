package com.example.color_walk.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ColorAnalysisResponse {
    @JsonProperty("dominantColors")
    private List<DominantColor> dominantColors;
    
    private String mood;
    
    @JsonProperty("harmonyScore")
    private Integer harmonyScore;
    
    @JsonProperty("recommendedTheme")
    private String recommendedTheme;
    
    private String description;
    
    @JsonProperty("individualImages")
    private List<IndividualImageAnalysis> individualImages = new ArrayList<>();
    
    private boolean success = true;
    private String errorMessage;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DominantColor {
        private String name;
        private String hex;
        private Double percentage;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndividualImageAnalysis {
        @JsonProperty("imageIndex")
        private Integer imageIndex;
        
        @JsonProperty("imageName")
        private String imageName;
        
        @JsonProperty("dominantColors")
        private List<DominantColor> dominantColors;
    }

    public static ColorAnalysisResponse fromJsonString(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String cleanedJson = jsonString.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.replaceFirst("```json", "").replaceFirst("```$", "").trim();
            }
            
            ColorAnalysisResponse response = mapper.readValue(cleanedJson, ColorAnalysisResponse.class);
            response.setSuccess(true);
            return response;
        } catch (JsonProcessingException e) {
            return createErrorResponse("JSON 파싱 오류: " + e.getMessage());
        }
    }

    public static ColorAnalysisResponse createErrorResponse(String errorMessage) {
        ColorAnalysisResponse response = new ColorAnalysisResponse();
        response.setSuccess(false);
        response.setErrorMessage(errorMessage);
        return response;
    }
}