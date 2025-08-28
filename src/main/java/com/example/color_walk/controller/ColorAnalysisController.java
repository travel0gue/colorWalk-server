package com.example.color_walk.controller;

import com.example.color_walk.dto.response.ColorAnalysisResponse;
import com.example.color_walk.service.ColorAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/colors")
@RequiredArgsConstructor
@Tag(name = "Color Analysis", description = "이미지 색상 분석 API")
public class ColorAnalysisController {

    private final ColorAnalysisService colorAnalysisService;

    /**
     * 이미지 색상 분석
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 색상 분석", description = "업로드된 이미지의 색상을 분석하여 주요 색상, 분위기, 조화도 등을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "색상 분석 성공", 
                    content = @Content(schema = @Schema(implementation = ColorAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일이 비어있거나 이미지 파일이 아님)",
                    content = @Content(schema = @Schema(implementation = ColorAnalysisResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ColorAnalysisResponse.class)))
    })
    public ResponseEntity<ColorAnalysisResponse> analyzeImageColors(
            @Parameter(description = "분석할 이미지 파일 (JPG, PNG 등)", required = true)
            @RequestParam("image") MultipartFile imageFile) {
        
        if (imageFile.isEmpty()) {
            ColorAnalysisResponse errorResponse = ColorAnalysisResponse.createErrorResponse("이미지 파일이 비어있습니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        if (!isImageFile(imageFile)) {
            ColorAnalysisResponse errorResponse = ColorAnalysisResponse.createErrorResponse("이미지 파일만 업로드 가능합니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            ColorAnalysisResponse response = colorAnalysisService.analyzeImageColors(imageFile);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ColorAnalysisResponse errorResponse = ColorAnalysisResponse.createErrorResponse("색상 분석 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}