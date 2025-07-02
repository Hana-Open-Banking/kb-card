package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_applications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardApplication extends DateTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 신청 고유번호
     */
    @Column(name = "application_id", unique = true, nullable = false, length = 20)
    private String applicationId;
    
    /**
     * 신청자 (FK to CardUser)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private CardUser applicant;
    
    /**
     * 신청 카드 상품 (FK to CardProduct)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", nullable = false)
    private CardProduct cardProduct;
    
    /**
     * 신청자명
     */
    @Column(name = "applicant_name", nullable = false, length = 50)
    private String applicantName;
    
    /**
     * 전화번호
     */
    @Column(name = "phone", nullable = false, length = 15)
    private String phone;
    
    /**
     * 이메일
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;
    
    /**ㅊ
     * 연수입
     */
    @Column(name = "annual_income", precision = 15, scale = 2)
    private BigDecimal annualIncome;
    
    /**
     * 신청 상태
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", nullable = false)
    @Builder.Default
    private ApplicationStatus applicationStatus = ApplicationStatus.PENDING;
    
    /**
     * 신청일시
     */
    @Column(name = "application_date", nullable = false)
    private LocalDateTime applicationDate;
    
    /**
     * 승인일시
     */
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    /**
     * 거절 사유
     */
    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;
    
    /**
     * 발급된 카드번호
     */
    @Column(name = "issued_card_no", length = 16)
    private String issuedCardNo;
    
    public enum ApplicationStatus {
        PENDING,    // 심사중
        APPROVED,   // 승인
        REJECTED    // 거절
    }
    
    public void approve(String cardNo) {
        this.applicationStatus = ApplicationStatus.APPROVED;
        this.approvedDate = LocalDateTime.now();
        this.issuedCardNo = cardNo;
    }
    
    public void reject(String reason) {
        this.applicationStatus = ApplicationStatus.REJECTED;
        this.rejectionReason = reason;
    }
    
    public boolean isApproved() {
        return applicationStatus == ApplicationStatus.APPROVED;
    }
    
    public boolean isPending() {
        return applicationStatus == ApplicationStatus.PENDING;
    }
    
    // 편의 메서드들
    public String getUserId() {
        return applicant != null ? applicant.getUserId() : null;
    }
    
    public String getUserCi() {
        return applicant != null ? applicant.getUserCi() : null;
    }
    
    public String getProductName() {
        return cardProduct != null ? cardProduct.getProductName() : null;
    }
    
    public String getCardType() {
        return cardProduct != null ? cardProduct.getCardType().name() : null;
    }
} 