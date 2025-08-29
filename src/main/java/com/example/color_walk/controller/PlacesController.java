package com.example.color_walk.controller;

import com.example.color_walk.domain.Places.PlaceCategory;
import com.example.color_walk.dto.request.PlaceCreateRequest;
import com.example.color_walk.dto.response.PlaceResponse;
import com.example.color_walk.service.PlacesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
@Tag(name = "Places", description = "장소 관리 API")
public class PlacesController {

    private final PlacesService placesService;

    @PostMapping
    @Operation(summary = "장소 추가", description = "새로운 장소를 등록합니다")
    @ApiResponse(responseCode = "201", description = "장소 등록 성공")
    public ResponseEntity<PlaceResponse> createPlace(@Valid @RequestBody PlaceCreateRequest request) {
        PlaceResponse response = placesService.createPlace(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{placeId}")
    @Operation(summary = "장소 삭제", description = "지정된 ID의 장소를 삭제합니다")
    @ApiResponse(responseCode = "204", description = "장소 삭제 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 장소")
    public ResponseEntity<Void> deletePlace(@PathVariable("placeId") Long placeId) {
        placesService.deletePlace(placeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{placeId}")
    @Operation(summary = "장소 상세 조회", description = "지정된 ID의 장소 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "장소 조회 성공")
    @ApiResponse(responseCode = "404", description = "존재하지 않는 장소")
    public ResponseEntity<PlaceResponse> getPlace(@PathVariable("placeId") Long placeId) {
        PlaceResponse response = placesService.getPlace(placeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "장소 목록 조회", description = "모든 장소 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "장소 목록 조회 성공")
    public ResponseEntity<List<PlaceResponse>> getAllPlaces() {
        List<PlaceResponse> responses = placesService.getAllPlaces();
        return ResponseEntity.ok(responses);
    }
}