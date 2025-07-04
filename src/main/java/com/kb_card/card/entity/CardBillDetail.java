package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "card_bill_details")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardBillDetail extends DateTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    /**
     * 청구서 정보 (FK to CardBill)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_bill_id", nullable = false)
    private CardBill cardBill;
    
    /**
     * 카드 식별자 (오픈뱅킹 API 명세)
     */
    @Column(name = "card_id", length = 64)
    private String cardId;
    
    /**
     * 사용일자 (YYYYMMDD)
     */
    @Column(name = "paid_date", nullable = false, length = 8)
    private String paidDate;
    
    /**
     * 사용시간 (HHMMSS)
     */
    @Column(name = "paid_time", nullable = false, length = 6)
    private String paidTime;
    
    /**
     * 이용금액 (원/KRW) - 마이너스 금액 가능
     */
    @Column(name = "paid_amt", nullable = false, precision = 13, scale = 0)
    private BigDecimal paidAmt;
    
    /**
     * 마스킹된 가맹점명
     */
    @Column(name = "merchant_name_masked", length = 40)
    private String merchantNameMasked;
    
    /**
     * 신용판매 수수료 (원/KRW) - 마이너스 금액 가능
     */
    @Column(name = "credit_fee_amt", precision = 13, scale = 0)
    @Builder.Default
    private BigDecimal creditFeeAmt = BigDecimal.ZERO;
    
    /**
     * 상품 구분
     * "01": 일시불
     * "02": 신용판매할부  
     * "03": 현금서비스
     */
    @Column(name = "product_type", length = 2)
    private String productType;
    
    // 편의 메서드들
    public String getCardNo() {
        return cardBill != null && cardBill.getCard() != null ? cardBill.getCard().getCardNo() : null;
    }
    
    public String getUserCi() {
        return cardBill != null && cardBill.getCard() != null ? cardBill.getCard().getUserCi() : null;
    }
    
    public String getChargeMonth() {
        return cardBill != null ? cardBill.getChargeMonth() : null;
    }
}