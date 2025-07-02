package com.kb_card.card.repository;

import com.kb_card.card.entity.CardProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardProductRepository extends JpaRepository<CardProduct, Long> {
    
    /**
     * 상품 코드로 카드 상품 조회
     */
    Optional<CardProduct> findByProductCode(String productCode);
    
    /**
     * 카드 유형별 상품 조회
     */
    List<CardProduct> findByCardTypeAndStatus(CardProduct.CardType cardType, CardProduct.ProductStatus status);
    
    /**
     * 판매중인 카드 상품 전체 조회
     */
    List<CardProduct> findByStatus(CardProduct.ProductStatus status);
    
    /**
     * 카드 유형별 판매중인 상품 조회
     */
    @Query("SELECT cp FROM CardProduct cp WHERE cp.cardType = :cardType AND cp.status = 'ACTIVE' ORDER BY cp.productName")
    List<CardProduct> findActiveProductsByCardType(@Param("cardType") CardProduct.CardType cardType);
    
    /**
     * 상품명으로 검색
     */
    List<CardProduct> findByProductNameContaining(String productName);
    
    /**
     * 카드 등급별 상품 조회
     */
    List<CardProduct> findByCardGradeAndStatus(CardProduct.CardGrade cardGrade, CardProduct.ProductStatus status);
} 