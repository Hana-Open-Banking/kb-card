package com.kb_card.card.repository;

import com.kb_card.card.entity.CardUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardUserRepository extends JpaRepository<CardUser, Long> {
    
    /**
     * 사용자 CI로 사용자 조회
     */
    Optional<CardUser> findByUserCi(String userCi);
    
    /**
     * 사용자 ID로 사용자 조회
     */
    Optional<CardUser> findByUserId(String userId);
    
    /**
     * 사용자 CI 존재 여부 확인
     */
    boolean existsByUserCi(String userCi);
    
    /**
     * 사용자 ID 존재 여부 확인
     */
    boolean existsByUserId(String userId);
} 