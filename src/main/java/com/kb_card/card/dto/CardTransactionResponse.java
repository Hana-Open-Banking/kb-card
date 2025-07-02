package com.kb_card.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CardTransactionResponse {
    
    /**
     * API 거래고유번호
     */
    @JsonProperty("api_tran_id")
    private String apiTranId;
    
    /**
     * API 거래일시
     */
    @JsonProperty("api_tran_dtm")
    private String apiTranDtm;
    
    /**
     * 응답코드
     */
    @JsonProperty("rsp_code")
    private String rspCode;
    
    /**
     * 응답메시지
     */
    @JsonProperty("rsp_message")
    private String rspMessage;
    
    /**
     * 은행거래고유번호
     */
    @JsonProperty("bank_tran_id")
    private String bankTranId;
    
    /**
     * 은행거래일자
     */
    @JsonProperty("bank_tran_date")
    private String bankTranDate;
    
    /**
     * 은행코드 (표준)
     */
    @JsonProperty("bank_code_tran")
    private String bankCodeTran;
    
    /**
     * 은행 응답코드
     */
    @JsonProperty("bank_rsp_code")
    private String bankRspCode;
    
    /**
     * 은행 응답메시지
     */
    @JsonProperty("bank_rsp_message")
    private String bankRspMessage;
    
    /**
     * 다음페이지 존재여부
     */
    @JsonProperty("next_page_yn")
    private String nextPageYn;
    
    /**
     * 직전조회추적정보
     */
    @JsonProperty("befor_inquiry_trace_info")
    private String beforInquiryTraceInfo;
    
    /**
     * 거래 개수
     */
    @JsonProperty("tran_cnt")
    private String tranCnt;
    
    /**
     * 거래 목록
     */
    @JsonProperty("tran_list")
    private List<TransactionInfo> tranList;
    
    @Data
    @Builder
    public static class TransactionInfo {
        /**
         * 거래 고유번호
         */
        @JsonProperty("tran_id")
        private String tranId;
        
        /**
         * 거래일자 (YYYYMMDD)
         */
        @JsonProperty("tran_date")
        private String tranDate;
        
        /**
         * 거래시간 (HHMMSS)
         */
        @JsonProperty("tran_time")
        private String tranTime;
        
        /**
         * 가맹점명
         */
        @JsonProperty("merchant_name")
        private String merchantName;
        
        /**
         * 가맹점 사업자번호
         */
        @JsonProperty("merchant_regno")
        private String merchantRegno;
        
        /**
         * 승인금액
         */
        @JsonProperty("approved_amt")
        private String approvedAmt;
        
        /**
         * 거래구분 (1:승인, 2:취소)
         */
        @JsonProperty("tran_type")
        private String tranType;
        
        /**
         * 거래 카테고리
         */
        @JsonProperty("category")
        private String category;
        
        /**
         * 메모
         */
        @JsonProperty("memo")
        private String memo;
    }
} 