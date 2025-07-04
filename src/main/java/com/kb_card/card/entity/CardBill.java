package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
     * 카드 정보 (FK to Card)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;
    
    /**
     * 청구년월 (YYYYMM) - 청구서가 결제되는 월
     */
    @Column(name = "charge_month", nullable = false, length = 6)
    private String chargeMonth;
    
    /**
     * 결제순번 - 동일 청구년월에 여러 건의 청구내역이 있을 때 구분용
     */
    @Column(name = "settlement_seq_no", nullable = false, length = 4)
    @Builder.Default
    private String settlementSeqNo = "0001";
    
    /**
     * 청구금액 (마이너스 금액 가능)
     */
    @Column(name = "charge_amt", nullable = false, precision = 13, scale = 0)
    @Builder.Default
    private BigDecimal chargeAmt = BigDecimal.ZERO;
    
    /**
     * 결제일 (고객이 지정한 결제일)
     */
    @Column(name = "settlement_day", nullable = false, length = 2)
    private String settlementDay;
    
    /**
     * 결제년월일 (실제 결제일, YYYYMMDD)
     * 휴일 등으로 실제 결제일은 고객이 지정한 결제일과 다를 수 있음
     */
    @Column(name = "settlement_date", length = 8)
    private String settlementDate;
    
    /**
     * 신용/체크 구분
     * "01": 신용
     * "02": 체크
     * "03": 신용/체크혼용
     */
    @Column(name = "credit_check_type", nullable = false, length = 2)
    @Builder.Default
    private String creditCheckType = "01";
    
    /**
     * 청구서 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "bill_status", nullable = false)
    @Builder.Default
    private BillStatus billStatus = BillStatus.ACTIVE;
    
    /**
     * 청구서 확정일 (다음 달 1일에 확정됨)
     */
    @Column(name = "closed_at")
    private LocalDate closedAt;
    
    /**
     * 청구서 상세 내역들
     */
    @OneToMany(mappedBy = "cardBill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CardBillDetail> billDetails = new ArrayList<>();
    
    public enum BillStatus {
        ACTIVE,     // 활성 (현재 달 청구서, 계속 업데이트됨)
        CLOSED,     // 확정 (이전 달 청구서, 더 이상 변경 없음)
        PAID,       // 결제완료
        OVERDUE     // 연체
    }
    
    /**
     * 청구서에 상세 내역 추가 및 총액 업데이트
     */
    public void addBillDetail(CardBillDetail detail) {
        this.billDetails.add(detail);
        this.chargeAmt = this.chargeAmt.add(detail.getPaidAmt());
    }
    
    /**
     * 청구서 총액 업데이트 (상세 내역 기준으로 재계산)
     */
    public void updateTotalAmount() {
        this.chargeAmt = billDetails.stream()
                .map(CardBillDetail::getPaidAmt)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 청구서 확정 처리
     */
    public void close() {
        this.billStatus = BillStatus.CLOSED;
        this.closedAt = LocalDate.now();
    }
    
    /**
     * 결제 완료 처리
     */
    public void markAsPaid(String actualSettlementDate) {
        this.billStatus = BillStatus.PAID;
        this.settlementDate = actualSettlementDate;
    }
    
    /**
     * 연체 처리
     */
    public void markAsOverdue() {
        this.billStatus = BillStatus.OVERDUE;
    }
    
    // 편의 메서드들
    public String getCardNo() {
        return card != null ? card.getCardNo() : null;
    }
    
    public String getUserCi() {
        return card != null ? card.getUserCi() : null;
    }
    
    public String getMemberId() {
        return card != null ? card.getUserId() : null;
    }
    
    public String getBankCodeStd() {
        return "381"; // KB카드 표준코드
    }
    
    public int getBillDetailCount() {
        return billDetails != null ? billDetails.size() : 0;
    }
    
    public boolean isActive() {
        return billStatus == BillStatus.ACTIVE;
    }
    
    public boolean isClosed() {
        return billStatus == BillStatus.CLOSED;
    }
}