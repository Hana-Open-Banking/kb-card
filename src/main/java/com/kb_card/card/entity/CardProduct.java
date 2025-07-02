package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "card_products")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardProduct extends DateTimeEntity {
    /**
     * 카드 상품 코드 (KB001, KB002 등)
     */
    @Id
    @Column(name = "product_code", unique = true, nullable = false, length = 10)
    private String productCode;
    
    /**
     * 카드 상품명
     */
    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;
    
    /**
     * 카드 유형
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;
    
    /**
     * 연회비
     */
    @Column(name = "annual_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal annualFee = BigDecimal.ZERO;
    
    /**
     * 카드 등급
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "card_grade", nullable = false)
    @Builder.Default
    private CardGrade cardGrade = CardGrade.STANDARD;
    
    /**
     * 카드 상품 설명
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 카드 상품 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;
    
    public enum CardType {
        CREDIT("신용카드"),
        DEBIT("체크카드"),
        PREPAID("선불카드");
        
        private final String description;
        
        CardType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum CardGrade {
        STANDARD("일반"),
        GOLD("골드"),
        PLATINUM("플래티넘"),
        BLACK("블랙");
        
        private final String description;
        
        CardGrade(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum ProductStatus {
        ACTIVE,      // 판매중
        INACTIVE,    // 판매중단
        DISCONTINUED // 단종
    }
    
    public boolean isSaleActive() {
        return status == ProductStatus.ACTIVE;
    }
    
    public void discontinue() {
        this.status = ProductStatus.DISCONTINUED;
    }
} 