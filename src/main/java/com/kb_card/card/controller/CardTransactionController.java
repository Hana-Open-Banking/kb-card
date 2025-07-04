package com.kb_card.card.controller;

import com.kb_card.card.entity.CardTransaction;
import com.kb_card.card.service.CardTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/transactions")
@RequiredArgsConstructor
public class CardTransactionController {
    
    private final CardTransactionService cardTransactionService;
    
    @PostMapping("/create")
    @Operation(summary = "거래 생성 (테스트용)", description = "새로운 거래를 생성하고 청구서에 자동 반영합니다.")
    public ResponseEntity<CreateTransactionResponse> createTransaction(@RequestBody CreateTransactionRequest request) {
        log.info("거래 생성 요청 - cardNo: {}, amount: {}, merchantName: {}", 
                request.getCardNo(), request.getAmount(), request.getMerchantName());
        
        try {
            // Request 검증
            if (request.getCardNo() == null || request.getCardNo().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(CreateTransactionResponse.error("카드번호가 필요합니다."));
            }
            
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(CreateTransactionResponse.error("유효한 금액이 필요합니다."));
            }
            
            if (request.getMerchantName() == null || request.getMerchantName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(CreateTransactionResponse.error("가맹점명이 필요합니다."));
            }
            
            // Service Request 생성
            CardTransactionService.CreateTransactionRequest serviceRequest = 
                    new CardTransactionService.CreateTransactionRequest();
            serviceRequest.setCardNo(request.getCardNo());
            serviceRequest.setAmount(request.getAmount());
            serviceRequest.setMerchantName(request.getMerchantName());
            serviceRequest.setMerchantRegno(request.getMerchantRegno());
            serviceRequest.setTranDate(request.getTranDate());
            serviceRequest.setTranTime(request.getTranTime());
            serviceRequest.setTranType(CardTransaction.TransactionType.APPROVAL);
            serviceRequest.setCategory(determineCategory(request.getMerchantName()));
            serviceRequest.setMemo(request.getMemo());
            
            CardTransaction transaction = cardTransactionService.createTransaction(serviceRequest);
            
            return ResponseEntity.ok(CreateTransactionResponse.success(transaction));
            
        } catch (Exception e) {
            log.error("거래 생성 실패", e);
            return ResponseEntity.badRequest().body(CreateTransactionResponse.error("거래 생성 실패: " + e.getMessage()));
        }
    }
    
    @GetMapping("/list/{cardNo}")
    @Operation(summary = "카드별 거래 내역 조회", description = "특정 카드의 모든 거래 내역을 조회합니다.")
    public ResponseEntity<List<CardTransaction>> getTransactionsByCardNo(@PathVariable String cardNo) {
        log.info("거래 내역 조회 요청 - cardNo: {}", cardNo);
        
        try {
            List<CardTransaction> transactions = cardTransactionService.getTransactionsByCardNo(cardNo);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("거래 내역 조회 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/list/{cardNo}/period")
    @Operation(summary = "기간별 거래 내역 조회", description = "특정 카드의 기간별 거래 내역을 조회합니다.")
    public ResponseEntity<List<CardTransaction>> getTransactionsByPeriod(
            @PathVariable String cardNo,
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        log.info("기간별 거래 내역 조회 요청 - cardNo: {}, fromDate: {}, toDate: {}", cardNo, fromDate, toDate);
        
        try {
            LocalDate from = LocalDate.parse(fromDate);
            LocalDate to = LocalDate.parse(toDate);
            
            List<CardTransaction> transactions = cardTransactionService.getTransactionsByCardNoAndDateRange(cardNo, from, to);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("기간별 거래 내역 조회 실패", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 가맹점명으로 카테고리 추정
     */
    private CardTransaction.TransactionCategory determineCategory(String merchantName) {
        if (merchantName == null) return CardTransaction.TransactionCategory.OTHERS;
        
        String name = merchantName.toLowerCase();
        
        if (name.contains("주유") || name.contains("gs") || name.contains("sk") || name.contains("에너지")) {
            return CardTransaction.TransactionCategory.FUEL;
        } else if (name.contains("통행료") || name.contains("도로공사") || name.contains("하이패스")) {
            return CardTransaction.TransactionCategory.TOLL;
        } else if (name.contains("주차") || name.contains("parking")) {
            return CardTransaction.TransactionCategory.PARKING;
        } else if (name.contains("정비") || name.contains("수리") || name.contains("카센터")) {
            return CardTransaction.TransactionCategory.MAINTENANCE;
        } else if (name.contains("마트") || name.contains("편의점") || name.contains("쇼핑")) {
            return CardTransaction.TransactionCategory.SHOPPING;
        } else if (name.contains("스타벅스") || name.contains("카페") || name.contains("음식") || name.contains("치킨")) {
            return CardTransaction.TransactionCategory.FOOD;
        } else {
            return CardTransaction.TransactionCategory.OTHERS;
        }
    }
    
    // DTO 클래스들
    
    public static class CreateTransactionRequest {
        private String cardNo;
        private BigDecimal amount;
        private String merchantName;
        private String merchantRegno;
        private LocalDate tranDate;
        private LocalTime tranTime;
        private String memo;
        
        // Getters and Setters
        public String getCardNo() { return cardNo; }
        public void setCardNo(String cardNo) { this.cardNo = cardNo; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getMerchantName() { return merchantName; }
        public void setMerchantName(String merchantName) { this.merchantName = merchantName; }
        
        public String getMerchantRegno() { return merchantRegno; }
        public void setMerchantRegno(String merchantRegno) { this.merchantRegno = merchantRegno; }
        
        public LocalDate getTranDate() { return tranDate; }
        public void setTranDate(LocalDate tranDate) { this.tranDate = tranDate; }
        
        public LocalTime getTranTime() { return tranTime; }
        public void setTranTime(LocalTime tranTime) { this.tranTime = tranTime; }
        
        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
    }
    
    public static class CreateTransactionResponse {
        private boolean success;
        private String message;
        private String transactionId;
        private String cardNo;
        private BigDecimal amount;
        
        public static CreateTransactionResponse success(CardTransaction transaction) {
            CreateTransactionResponse response = new CreateTransactionResponse();
            response.success = true;
            response.message = "거래 생성 완료";
            response.transactionId = transaction.getTransactionId();
            response.cardNo = transaction.getCardNo();
            response.amount = transaction.getApprovedAmt();
            return response;
        }
        
        public static CreateTransactionResponse error(String message) {
            CreateTransactionResponse response = new CreateTransactionResponse();
            response.success = false;
            response.message = message;
            return response;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getCardNo() { return cardNo; }
        public void setCardNo(String cardNo) { this.cardNo = cardNo; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}