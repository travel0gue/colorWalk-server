package com.example.color_walk.service;

import com.example.color_walk.common.util.ExifUtils;
import com.example.color_walk.domain.Photo;
import com.example.color_walk.domain.Walk;
import com.example.color_walk.dto.response.PhotoResponse;
import com.example.color_walk.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.color_walk.repository.WalkRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final WalkRepository walkRepository;
    private final S3Service s3Service;

    public List<PhotoResponse> uploadPhotos(Long walkId, List<MultipartFile> files, List<String> descriptions) {
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new RuntimeException("Walk not found with id: " + walkId));

        List<Photo> savedPhotos = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String description = (descriptions != null && i < descriptions.size()) ? descriptions.get(i) : null;

            try {
                // 1. 이미지에서 GPS 정보 추출
                ExifUtils.LocationInfo locationInfo = ExifUtils.extractLocationFromImage(file);

                // 2. S3에 파일 업로드
                String s3Url = s3Service.uploadFile(file, "photos");

                // 3. Photo 엔티티 생성 및 저장
                Photo photo = Photo.builder()
                        .walk(walk)
                        .s3Url(s3Url)
                        .latitude(locationInfo.getLatitude())
                        .longitude(locationInfo.getLongitude())
                        .description(description)
                        .displayOrder(i + 1)
                        .build();

                Photo savedPhoto = photoRepository.save(photo);
                savedPhotos.add(savedPhoto);

                log.info("사진 업로드 완료 - Walk ID: {}, S3 URL: {}, GPS: ({}, {})",
                        walkId, s3Url, locationInfo.getLatitude(), locationInfo.getLongitude());

            } catch (Exception e) {
                log.error("사진 업로드 중 오류 발생 - 파일명: {}, 오류: {}",
                        file.getOriginalFilename(), e.getMessage(), e);
                throw new RuntimeException("사진 업로드 실패: " + file.getOriginalFilename(), e);
            }
        }

        return savedPhotos.stream()
                .map(PhotoResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> getPhotosByWalkId(Long walkId) {
        Walk walk = walkRepository.findById(walkId)
                .orElseThrow(() -> new RuntimeException("Walk not found with id: " + walkId));

        List<Photo> photos = photoRepository.findByWalkOrderByDisplayOrderAsc(walk);

        // Entity -> DTO 변환
        return photos.stream()
                .map(PhotoResponse::from)
                .collect(Collectors.toList());
    }

    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found with id: " + photoId));

        try {
            // S3에서 파일 삭제 (URL에서 키 추출)
            String s3Key = extractS3KeyFromUrl(photo.getS3Url());
            s3Service.deleteFile(s3Key);
        } catch (Exception e) {
            log.warn("S3 파일 삭제 실패: {}", e.getMessage());
        }

        photoRepository.delete(photo);
        log.info("사진 삭제 완료 - Photo ID: {}", photoId);
    }

    private String extractS3KeyFromUrl(String s3Url) {
        // S3 URL에서 키 부분만 추출
        // 예: https://bucket-name.s3.region.amazonaws.com/photos/uuid.jpg -> photos/uuid.jpg
        return s3Url.substring(s3Url.indexOf(".com/") + 5);
    }
}
