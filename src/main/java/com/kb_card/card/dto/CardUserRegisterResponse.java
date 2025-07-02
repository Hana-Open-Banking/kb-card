package com.kb_card.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUserRegisterResponse {
    
    @JsonProperty("api_tran_id")
    private String apiTranId;
    
    @JsonProperty("api_tran_dtm")
    private String apiTranDtm;
    
    @JsonProperty("rsp_code")
    private String rspCode;
    
    @JsonProperty("rsp_message")
    private String rspMessage;
    
    @JsonProperty("bank_tran_id")
    private String bankTranId;
    
    @JsonProperty("bank_tran_date")
    private String bankTranDate;
    
    @JsonProperty("bank_code_tran")
    private String bankCodeTran;
    
    @JsonProperty("bank_rsp_code")
    private String bankRspCode;
    
    @JsonProperty("bank_rsp_message")
    private String bankRspMessage;
    
    @JsonProperty("bank_name")
    private String bankName;
    
    @JsonProperty("user_seq_no")
    private String userSeqNo;
} 