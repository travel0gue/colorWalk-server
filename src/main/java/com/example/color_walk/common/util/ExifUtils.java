package com.example.color_walk.common.util;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;

import com.drew.metadata.exif.GpsDirectory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class ExifUtils {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
        private BigDecimal latitude;
        private BigDecimal longitude;

        public boolean hasLocation() {
            return latitude != null && longitude != null;
        }
    }

    public static LocationInfo extractLocationFromImage(MultipartFile file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            GpsDirectory gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);

            if (gpsDirectory == null) {
                log.info("GPS 정보가 없는 이미지입니다: {}", file.getOriginalFilename());
                return new LocationInfo(null, null);
            }

            com.drew.lang.GeoLocation geoLocation = gpsDirectory.getGeoLocation();

            BigDecimal latitude = BigDecimal.valueOf(geoLocation.getLatitude())
                    .setScale(8, RoundingMode.HALF_UP);
            BigDecimal longitude = BigDecimal.valueOf(geoLocation.getLongitude())
                    .setScale(8, RoundingMode.HALF_UP);

            log.info("이미지에서 GPS 정보 추출 완료 - 위도: {}, 경도: {}", latitude, longitude);
            return new LocationInfo(latitude, longitude);

        } catch (Exception e) {
            log.error("이미지 메타데이터 추출 중 오류 발생: {}", e.getMessage(), e);
            return new LocationInfo(null, null);
        }
    }
}
