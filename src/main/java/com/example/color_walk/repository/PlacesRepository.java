package com.example.color_walk.repository;

import com.example.color_walk.domain.Places;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PlacesRepository extends JpaRepository<Places, Long> {
    
    List<Places> findByCategory(Places.PlaceCategory category);
    
    List<Places> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT p FROM Places p WHERE p.latitude BETWEEN :minLat AND :maxLat AND p.longitude BETWEEN :minLng AND :maxLng")
    List<Places> findPlacesInArea(@Param("minLat") BigDecimal minLatitude,
                                  @Param("maxLat") BigDecimal maxLatitude,
                                  @Param("minLng") BigDecimal minLongitude,
                                  @Param("maxLng") BigDecimal maxLongitude);
}