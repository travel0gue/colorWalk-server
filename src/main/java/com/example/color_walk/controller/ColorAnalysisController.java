package com.example.color_walk.controller;

import com.example.color_walk.dto.response.ColorAnalysisResponse;
import com.example.color_walk.dto.response.ColorAnalysisResponseWithPoint;
import com.example.color_walk.service.ColorAnalysisService;
import com.example.color_walk.service.WalkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

@RestController
@RequestMapping("/colors")
@RequiredArgsConstructor
@Tag(name = "Color Analysis", description = "이미지 색상 분석 API")
public class ColorAnalysisController {

    private final ColorAnalysisService colorAnalysisService;
    private final WalkService walkService;

    /**
     * 여러 이미지 종합 색상 분석
     */
    @PostMapping(value = "/analyze/{walkId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "여러 이미지 종합 색상 분석", description = "업로드된 여러 이미지를 종합적으로 분석하여 전체적인 색상 테마, 분위기, 조화도 등을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "색상 분석 성공", 
                    content = @Content(schema = @Schema(implementation = ColorAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (파일이 비어있거나 이미지 파일이 아님)",
                    content = @Content(schema = @Schema(implementation = ColorAnalysisResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = ColorAnalysisResponse.class)))
    })
    public ResponseEntity<ColorAnalysisResponse> analyzeImageColors(
            @PathVariable("walkId") Long walkId,
            @Parameter(
                    description = "분석할 이미지 파일들 (JPG, PNG 등) - 여러 개 선택 가능",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE),
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("images") List<MultipartFile> imageFiles) {
        
        if (imageFiles == null || imageFiles.toArray().length == 0) {
            ColorAnalysisResponse errorResponse = ColorAnalysisResponse.createErrorResponse("최소 1개 이상의 이미지 파일이 필요합니다.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // 모든 파일이 이미지 파일인지 검증
        for (MultipartFile file : imageFiles) {
            if (file.isEmpty()) {
                ColorAnalysisResponse errorResponse = ColorAnalysisResponse.createErrorResponse("비어있는 이미지 파일이 있습니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (!isImageFile(file)) {
                ColorAnalysisResponse errorResponse = ColorAnalysisResponse.createErrorResponse("이미지 파일만 업로드 가능합니다: " + file.getOriginalFilename());
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
        
        try {
            ColorAnalysisResponse response;
            
            if (imageFiles.toArray().length == 1) {
                // 단일 이미지인 경우 기존 메소드 사용
                response = colorAnalysisService.analyzeImageColors(imageFiles.get(0));
            } else {
                // 여러 이미지인 경우 종합 분석 메소드 사용
                response = colorAnalysisService.analyzeMultipleImageColors(imageFiles);
            }

            Integer gainedPoint;
            if (response.getIndividualImages().isEmpty()){
                gainedPoint = -1;
            }else {
                gainedPoint = walkService.calculatePoints(walkId, response.getIndividualImages());
            }
            ColorAnalysisResponseWithPoint responseWithPoint = new ColorAnalysisResponseWithPoint(response, gainedPoint);
            System.out.println("gainedPoint: " + gainedPoint);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ColorAnalysisResponse errorResponse = ColorAnalysisResponse.createErrorResponse("색상 분석 중 오류가 발생했습니다!!: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}