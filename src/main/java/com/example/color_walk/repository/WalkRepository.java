package com.example.color_walk.repository;

import com.example.color_walk.domain.Member;
import com.example.color_walk.domain.Walk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalkRepository extends JpaRepository<Walk, Long> {
    
    List<Walk> findByMemberOrderByCreatedAtDesc(Member member);
    
    List<Walk> findByMemberIdOrderByCreatedAtDesc(Long memberId);
    
    @Query("SELECT w FROM Walk w WHERE w.member.id = :memberId")
    List<Walk> findWalksByMemberId(@Param("memberId") Long memberId);
    
    List<Walk> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT w FROM Walk w LEFT JOIN FETCH w.walkingPoints WHERE w.id = :walkId")
    Optional<Walk> findByIdWithPoints(@Param("walkId") Long walkId);

    List<Walk> findByMemberIdOrderByStartTimeDesc(Long memberId);

    @Modifying
    @Query("UPDATE Walk w SET w.totalDistance = :totalDistance WHERE w.id = :walkId")
    void updateTotalDistance(@Param("walkId") Long walkId, @Param("totalDistance") Double totalDistance);
}