package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "card_bills")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardBill extends DateTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 청구서 고유번호
     */
    @Column(name = "bill_id", unique = true, nullable = false, length = 20)
    private String billId;
    
    /**
     * 카드 정보 (FK to Card)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    /**
     * 청구월 (YYYYMM 형식)
     */
    @Column(name = "bill_month", nullable = false, length = 6)
    private String billMonth;
    
    /**
     * 청구금액
     */
    @Column(name = "bill_amt", nullable = false, precision = 15, scale = 2)
    private BigDecimal billAmt;
    
    /**
     * 납부기한
     */
    @Column(name = "pay_due_date", nullable = false)
    private LocalDate payDueDate;
    
    /**
     * 청구서 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "bill_status", nullable = false)
    @Builder.Default
    private BillStatus billStatus = BillStatus.UNPAID;
    
    /**
     * 연체료
     */
    @Column(name = "late_fee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;
    
    /**
     * 납부금액
     */
    @Column(name = "paid_amt", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal paidAmt = BigDecimal.ZERO;
    
    /**
     * 납부일자
     */
    @Column(name = "paid_date")
    private LocalDate paidDate;
    
    /**
     * 국내이용금액
     */
    @Column(name = "domestic_amt", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal domesticAmt = BigDecimal.ZERO;
    
    /**
     * 해외이용금액
     */
    @Column(name = "overseas_amt", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal overseasAmt = BigDecimal.ZERO;
    
    /**
     * 현금서비스금액
     */
    @Column(name = "cash_service_amt", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal cashServiceAmt = BigDecimal.ZERO;
    
    public enum BillStatus {
        UNPAID("1"),    // 미납
        PAID("2"),      // 완납
        PARTIAL("3"),   // 부분납부
        OVERDUE("4");   // 연체
        
        private final String code;
        
        BillStatus(String code) {
            this.code = code;
        }
        
        public String getCode() {
            return code;
        }
    }
    
    public void markAsPaid(BigDecimal amount, LocalDate paymentDate) {
        this.paidAmt = amount;
        this.paidDate = paymentDate;
        
        if (amount.compareTo(billAmt) >= 0) {
            this.billStatus = BillStatus.PAID;
        } else {
            this.billStatus = BillStatus.PARTIAL;
        }
    }
    
    public void markAsOverdue(BigDecimal lateFeeAmount) {
        this.billStatus = BillStatus.OVERDUE;
        this.lateFee = lateFeeAmount;
    }
    
    public boolean isOverdue() {
        return billStatus == BillStatus.OVERDUE || 
               (billStatus == BillStatus.UNPAID && LocalDate.now().isAfter(payDueDate));
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