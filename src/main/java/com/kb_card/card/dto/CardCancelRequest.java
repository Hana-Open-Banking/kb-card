package com.kb_card.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "카드조회해지 요청")
public class CardCancelRequest {
    
    @JsonProperty("bank_tran_id")
    @NotBlank
    @Size(max = 20)
    @Schema(description = "거래고유번호(참가기관)", example = "F123456789U4BC34239Z")
    private String bankTranId;
    
    @JsonProperty("user_seq_no")
    @NotBlank
    @Size(max = 10)
    @Schema(description = "사용자일련번호", example = "U123456789")
    private String userSeqNo;
    
    @JsonProperty("bank_code_std")
    @NotBlank
    @Size(max = 3)
    @Schema(description = "조회 대상 카드사 대표코드 (금융기관 공동코드)", example = "091")
    private String bankCodeStd;
    
    @JsonProperty("member_bank_code")
    @NotBlank
    @Size(max = 3)
    @Schema(description = "조회 대상 회원 금융회사 코드 (금융기관 공동코드)", example = "091")
    private String memberBankCode;
    
    /**
     * 사용자 CI (금결원에서 카드사로 전달)
     */
    @NotBlank(message = "사용자 CI는 필수입니다.")
    private String userCi;
} 