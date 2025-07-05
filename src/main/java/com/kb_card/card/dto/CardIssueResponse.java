package com.kb_card.card.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CardIssueResponse {
    
    /**
     * 카드 발급 신청 번호
     */
    private String applicationId;
    
    /**
     * 카드 상품명
     */
    private String cardProductName;

    /**
     * 카드 이미지
     */
    private String cardImage;
    
    /**
     * 발급 상태 (PENDING: 신청중, APPROVED: 승인, REJECTED: 거절)
     */
    private String issueStatus;
    
    /**
     * 예상 발급일 (승인된 경우)
     */
    private String expectedIssueDate;
    
    /**
     * 배송 예정일 (승인된 경우)
     */
    private String expectedDeliveryDate;
    
    /**
     * 발급 결과 메시지
     */
    private String message;
    
    /**
     * 다음 단계 URL (추가 서류 제출 등)
     */
    private String nextStepUrl;
} 