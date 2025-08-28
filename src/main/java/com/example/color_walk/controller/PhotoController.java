package com.example.color_walk.controller;

import com.example.color_walk.dto.response.PhotoResponse;
import com.example.color_walk.service.PhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(value = "/upload/{walkId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<PhotoResponse>> uploadPhotos(
            @PathVariable("walkId") Long walkId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "descriptions", required = false) List<String> descriptions) {
        log.info("사진 업로드 요청 - Walk ID: {}, 파일 개수: {}", walkId, files.size());

        List<PhotoResponse> response = photoService.uploadPhotos(walkId, files, descriptions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/walk/{walkId}")
    public ResponseEntity<List<PhotoResponse>> getPhotosByWalkId(
            @PathVariable("walkId") Long walkId) {
        log.info("산책별 사진 조회 요청 - Walk ID: {}", walkId);

        List<PhotoResponse> response = photoService.getPhotosByWalkId(walkId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        log.info("사진 삭제 요청 - Photo ID: {}", photoId);

        photoService.deletePhoto(photoId);
        return ResponseEntity.noContent().build();
    }
}
