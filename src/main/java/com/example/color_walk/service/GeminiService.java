package com.example.color_walk.service;

import com.example.color_walk.dto.request.GeminiRequest;
import com.example.color_walk.dto.response.GeminiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final RestTemplate restTemplate;

    public GeminiRequest buildGeminiRequestWithImage(String prompt, String base64Image, String mimeType) {
        GeminiRequest.InlineData inlineData = new GeminiRequest.InlineData(mimeType, base64Image);

        GeminiRequest.Part textPart = new GeminiRequest.Part(prompt);
        GeminiRequest.Part imagePart = new GeminiRequest.Part(inlineData);

        GeminiRequest.Content content = new GeminiRequest.Content(List.of(textPart, imagePart));

        return new GeminiRequest(List.of(content));
    }

    public GeminiRequest buildGeminiRequestWithTextOnly(String prompt) {
        GeminiRequest.Part textPart = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(List.of(textPart));
        
        return new GeminiRequest(List.of(content));
    }

    public GeminiResponse callGeminiApi(GeminiRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);
        String url = geminiApiUrl + "?key=" + geminiApiKey;

        ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, GeminiResponse.class
        );

        if (response.getBody() == null || response.getBody().getCandidates().isEmpty()) {
            throw new RuntimeException("Gemini API 응답이 비어있습니다.");
        }

        return response.getBody();
    }
}
