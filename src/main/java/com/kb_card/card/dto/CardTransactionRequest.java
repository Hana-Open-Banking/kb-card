package com.kb_card.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "카드거래내역조회 요청")
public class CardTransactionRequest {
    
    /**
     * 은행거래고유번호 (이용기관에서 생성)
     */
    @NotBlank(message = "은행거래고유번호는 필수입니다.")
    private String bankTranId;
    
    /**
     * 사용자 CI (카드사에서는 CI 사용)
     */
    @NotBlank(message = "사용자 CI는 필수입니다.")
    private String userCi;
    
    /**
     * 카드사 대표코드 (금융기관 공동코드)
     */
    @NotBlank(message = "카드사 대표코드는 필수입니다.")
    private String bankCodeStd;
    
    /**
     * 회원 금융회사 코드 (금융기관 공동코드)
     */
    @NotBlank(message = "회원 금융회사 코드는 필수입니다.")
    private String memberBankCode;
    
    /**
     * 카드 식별자
     */
    @NotBlank(message = "카드 식별자는 필수입니다.")
    private String cardId;
    
    /**
     * 조회 시작일자 (YYYYMMDD)
     */
    @NotBlank(message = "조회 시작일자는 필수입니다.")
    private String fromDate;
    
    /**
     * 조회 종료일자 (YYYYMMDD)
     */
    @NotBlank(message = "조회 종료일자는 필수입니다.")
    private String toDate;
    
    /**
     * 페이지 인덱스 (1부터 시작)
     */
    private String pageIndex = "1";
    
    /**
     * 직전조회추적정보
     */
    private String beforInquiryTraceInfo;
} 