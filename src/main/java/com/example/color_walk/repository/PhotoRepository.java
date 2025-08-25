package com.example.color_walk.repository;

import com.example.color_walk.domain.Photo;
import com.example.color_walk.domain.Walk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    List<Photo> findByWalkOrderByDisplayOrderAsc(Walk walk);
    
    List<Photo> findByWalkIdOrderByDisplayOrderAsc(Long walkId);
    
    @Query("SELECT p FROM Photo p WHERE p.walk.id = :walkId ORDER BY p.displayOrder ASC")
    List<Photo> findPhotosByWalkId(@Param("walkId") Long walkId);
}