package com.example.color_walk.service;

import com.example.color_walk.domain.Places;
import com.example.color_walk.dto.request.PlaceCreateRequest;
import com.example.color_walk.dto.response.PlaceResponse;
import com.example.color_walk.repository.PlacesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlacesService {

    private final PlacesRepository placesRepository;

    @Transactional
    public PlaceResponse createPlace(PlaceCreateRequest request) {
        Places place = Places.builder()
                .name(request.getName())
                .description(request.getDescription())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .address(request.getAddress())
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .build();

        Places savedPlace = placesRepository.save(place);
        return PlaceResponse.from(savedPlace);
    }

    @Transactional
    public void deletePlace(Long placeId) {
        Places place = placesRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. ID: " + placeId));
        
        placesRepository.delete(place);
    }

    public PlaceResponse getPlace(Long placeId) {
        Places place = placesRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장소입니다. ID: " + placeId));
        
        return PlaceResponse.from(place);
    }

    public List<PlaceResponse> getAllPlaces() {
        return placesRepository.findAll().stream()
                .map(PlaceResponse::from)
                .collect(Collectors.toList());
    }
}