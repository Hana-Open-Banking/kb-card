package com.kb_card.card.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CardIssueRequest {
    
    /**
     * 사용자 CI
     */
    @NotBlank(message = "사용자 CI는 필수입니다.")
    private String userCi;
    
    /**
     * 카드 종류 (CREDIT: 신용카드, CHECK: 체크카드)
     */
    @NotBlank(message = "카드 종류는 필수입니다.")
    @Pattern(regexp = "CREDIT|CHECK", message = "카드 종류는 CREDIT 또는 CHECK만 가능합니다.")
    private String cardType;
    
    /**
     * 카드 상품명
     */
    @NotBlank(message = "카드 상품명은 필수입니다.")
    private String cardProductName;
    
    /**
     * 연간 소득 (원)
     */
    private Long annualIncome;
    
    /**
     * 배송지 주소
     */
    @NotBlank(message = "배송지 주소는 필수입니다.")
    private String deliveryAddress;
    
    /**
     * 연락처
     */
    @NotBlank(message = "연락처는 필수입니다.")
    @Pattern(regexp = "\\d{3}-\\d{4}-\\d{4}", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String phoneNumber;
    
    /**
     * 이메일
     */
    private String email;
    
    /**
     * 리다이렉트 URL (카드 발급 완료 후 돌아갈 URL)
     */
    @NotBlank(message = "리다이렉트 URL은 필수입니다.")
    private String redirectUrl;
} 