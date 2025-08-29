package com.example.color_walk.repository;

import com.example.color_walk.domain.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.color_walk.domain.Character;

import java.util.List;

@Repository
public interface CharacterRepository extends JpaRepository<com.example.color_walk.domain.Character, Long> {
    List<Character> findByColor(Color color);
}
