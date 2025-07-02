package com.kb_card.card.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUserRegisterRequest {
    
    @NotBlank
    @Size(max = 20)
    @JsonProperty("bank_tran_id")
    private String bankTranId;
    
    @NotBlank
    @Size(max = 3)
    @JsonProperty("bank_code_std")
    private String bankCodeStd;
    
    @NotBlank
    @Size(max = 3)
    @JsonProperty("member_bank_code")
    private String memberBankCode;
    
    @NotBlank
    @Size(max = 20)
    @JsonProperty("user_name")
    private String userName;
    
    @NotBlank
    @Size(max = 100)
    @JsonProperty("user_ci")
    private String userCi;
    
    @NotBlank
    @Email
    @Size(max = 100)
    @JsonProperty("user_email")
    private String userEmail;
    
    @NotBlank
    @Pattern(regexp = "cardinfo", message = "scope는 cardinfo만 허용됩니다")
    private String scope;
    
    @NotBlank
    @Pattern(regexp = "Y", message = "제3자정보제공동의여부는 Y만 허용됩니다")
    @JsonProperty("info_prvd_agmt_yn")
    private String infoPrvdAgmtYn;
} 