package com.kb_card.card.repository;

import com.kb_card.card.entity.Card;
import com.kb_card.card.entity.CardUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    /**
     * 사용자별 카드 조회 (CardUser 관계 사용)
     */
    List<Card> findByCardUser(CardUser cardUser);
    
    /**
     * 사용자 ID로 카드 조회
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardUser.userId = :userId")
    List<Card> findByUserId(@Param("userId") String userId);
    
    /**
     * 사용자 CI로 카드 조회 (호환성 유지)
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardUser.userCi = :userCi")
    List<Card> findByUserCi(@Param("userCi") String userCi);
    
    /**
     * 카드번호로 카드 조회
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardNo = :cardNo")
    Optional<Card> findByCardNo(@Param("cardNo") String cardNo);

    /**
     * 사용자별 유효한 카드 조회 (해지되지 않은 카드)
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardUser.userId = :userId AND c.cardStatus != 'CLOSED'")
    List<Card> findValidCardsByUserId(@Param("userId") String userId);
    
    /**
     * 사용자 CI별 유효한 카드 조회 (호환성 유지)
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardUser.userCi = :userCi AND c.cardStatus != 'CLOSED'")
    List<Card> findValidCardsByUserCi(@Param("userCi") String userCi);
    
    /**
     * 사용자별 활성 카드 조회
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardUser.userId = :userId AND c.cardStatus = 'NORMAL'")
    List<Card> findActiveCardsByUserId(@Param("userId") String userId);
    
    /**
     * 사용자 CI별 활성 카드 조회 (호환성 유지)
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardUser.userCi = :userCi AND c.cardStatus = 'NORMAL'")
    List<Card> findActiveCardsByUserCi(@Param("userCi") String userCi);
    
    /**
     * 카드 상태별 조회
     */
    @Query("SELECT c FROM Card c JOIN FETCH c.cardProduct WHERE c.cardStatus = :cardStatus")
    List<Card> findByCardStatus(@Param("cardStatus") Card.CardStatus cardStatus);
    
    /**
     * 카드번호 존재 여부 확인
     */
    boolean existsByCardNo(String cardNo);
    
    /**
     * 사용자별 카드 존재 여부 확인
     */
    @Query("SELECT COUNT(c) > 0 FROM Card c WHERE c.cardUser.userId = :userId AND c.cardStatus != 'CLOSED'")
    boolean existsValidCardsByUserId(@Param("userId") String userId);
} 