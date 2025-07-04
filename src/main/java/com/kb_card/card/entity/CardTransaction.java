package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "card_transactions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransaction extends DateTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 거래 고유번호
     */
    @Column(name = "transaction_id", unique = true, nullable = false, length = 30)
    private String transactionId;
    
    /**
     * 카드 정보 (FK to Card)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    /**
     * 거래 날짜
     */
    @Column(name = "tran_date", nullable = false)
    private LocalDate tranDate;
    
    /**
     * 거래 시간
     */
    @Column(name = "tran_time", nullable = false)
    private LocalTime tranTime;
    
    /**
     * 가맹점명
     */
    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;
    
    /**
     * 가맹점 사업자번호
     */
    @Column(name = "merchant_regno", length = 20)
    private String merchantRegno;
    
    /**
     * 승인금액
     */
    @Column(name = "approved_amt", nullable = false, precision = 15, scale = 2)
    private BigDecimal approvedAmt;
    
    /**
     * 거래구분
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tran_type", nullable = false)
    private TransactionType tranType;
    
    /**
     * 거래 카테고리
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private TransactionCategory category;
    
    /**
     * 메모
     */
    @Column(name = "memo", length = 200)
    private String memo;
    
    public enum TransactionType {
        APPROVAL("1"),  // 승인
        CANCEL("2");    // 취소
        
        private final String code;
        
        TransactionType(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    public enum TransactionCategory {
        FUEL,           // 주유
        TOLL,           // 통행료  
        PARKING,        // 주차
        MAINTENANCE,    // 정비
        SHOPPING,       // 쇼핑
        FOOD,           // 음식
        OTHERS          // 기타
    }
    
    public boolean isCarRelated() {
        return category == TransactionCategory.FUEL || 
               category == TransactionCategory.TOLL || 
               category == TransactionCategory.PARKING || 
               category == TransactionCategory.MAINTENANCE;
    }
    
    // 편의 메서드들
    public String getCardNo() {
        return card != null ? card.getCardNo() : null;
    }
    
    public String getUserId() {
        return card != null ? card.getUserId() : null;
    }
    
    public String getUserCi() {
        return card != null ? card.getUserCi() : null;
    }
    
    public String getCardName() {
        return card != null ? card.getCardName() : null;
    }
} 