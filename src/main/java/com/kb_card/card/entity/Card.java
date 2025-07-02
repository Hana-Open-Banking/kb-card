package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card extends DateTimeEntity {
    
    @Id
    @Column(name = "card_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 카드번호
     */
    @Column(name = "card_no", unique = true, nullable = false, length = 16)
    private String cardNo;
    
    /**
     * 카드 소유자 (FK to CardUser)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private CardUser cardUser;
    
    /**
     * 카드 상품 (FK to CardProduct)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", nullable = false)
    private CardProduct cardProduct;
    
    /**
     * 카드 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "card_status", nullable = false)
    @Builder.Default
    private CardStatus cardStatus = CardStatus.NORMAL;
    
    /**
     * 카드 발급일
     */
    @Column(name = "issue_date")
    private LocalDate issueDate;
    
    /**
     * 카드 만료일
     */
    @Column(name = "expire_date")
    private LocalDate expireDate;
    
    /**
     * 카드 별칭 (사용자 지정)
     */
    @Column(name = "card_alias", length = 50)
    private String cardAlias;
    
    public enum CardStatus {
        NORMAL,     // 정상
        LOST,       // 분실
        STOPPED,    // 정지  
        ACCIDENT,   // 사고
        CLOSED      // 해지
    }
    
    public boolean isValidCard() {
        return cardStatus != CardStatus.CLOSED;
    }
    
    public void reportLost() {
        this.cardStatus = CardStatus.LOST;
    }
    
    public void stopCard() {
        this.cardStatus = CardStatus.STOPPED;
    }
    
    public void activateCard() {
        this.cardStatus = CardStatus.NORMAL;
    }
    
    public void closeCard() {
        this.cardStatus = CardStatus.CLOSED;
    }
    
    public void updateAlias(String alias) {
        this.cardAlias = alias;
    }
    
    // 편의 메서드들
    public String getCardName() {
        return cardProduct != null ? cardProduct.getProductName() : "Unknown Card";
    }
    
    public String getCardType() {
        return cardProduct != null ? cardProduct.getCardType().name() : "UNKNOWN";
    }
    
    public String getUserId() {
        return cardUser != null ? cardUser.getUserId() : null;
    }
    
    public String getUserCi() {
        return cardUser != null ? cardUser.getUserCi() : null;
    }
} 