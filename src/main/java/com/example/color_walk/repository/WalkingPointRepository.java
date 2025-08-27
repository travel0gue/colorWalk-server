package com.example.color_walk.repository;

import com.example.color_walk.domain.WalkingPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalkingPointRepository extends JpaRepository<WalkingPoint, Long> {

    List<WalkingPoint> findByWalkIdOrderBySequence(Long walkId);

    int countByWalkId(Long walkId);
}
