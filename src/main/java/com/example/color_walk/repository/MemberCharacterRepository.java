package com.example.color_walk.repository;

import com.example.color_walk.domain.Character;
import com.example.color_walk.domain.Color;
import com.example.color_walk.domain.Member;
import com.example.color_walk.domain.MemberCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberCharacterRepository extends JpaRepository<MemberCharacter, Long> {
    List<MemberCharacter> findByMember(Member member);
    List<MemberCharacter> findByMemberId(Long memberId);
    Optional<MemberCharacter> findByMemberAndCharacter(Member member, Character character);
    List<MemberCharacter> findByMemberAndCharacter_Color(Member member, Color color);
}
