package com.kb_card.card.service;

import com.kb_card.card.entity.Card;
import com.kb_card.card.entity.CardTransaction;
import com.kb_card.card.repository.CardRepository;
import com.kb_card.card.repository.CardTransactionRepository;
import com.kb_card.common.exception.BusinessException;
import com.kb_card.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardTransactionService {
    
    private final CardTransactionRepository cardTransactionRepository;
    private final CardRepository cardRepository;
    private final CardBillService cardBillService;
    private final CardBillTransactionService cardBillTransactionService;
    
    /**
     * 새로운 거래 생성 및 청구서 반영 (cardNo 기반)
     */
    @Transactional
    public CardTransaction createTransaction(CreateTransactionRequest request) {
        log.info("거래 생성 시작 - cardNo: {}, amount: {}, merchantName: {}", 
                request.getCardNo(), request.getAmount(), request.getMerchantName());
        
        try {
            // 1. 카드 조회 (cardNo 기반)
            Card card = cardRepository.findByCardNo(request.getCardNo())
                    .orElseThrow(() -> {
                        log.error("카드를 찾을 수 없음 - cardNo: {}", request.getCardNo());
                        return new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
                    });
            
            // 2. 카드 유효성 검증
            if (!card.isValidCard()) {
                log.error("유효하지 않은 카드 - cardNo: {}, status: {}", request.getCardNo(), card.getCardStatus());
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            
            // 3. 거래 ID 생성
            String transactionId = generateTransactionId();
            
            // 4. 거래 엔티티 생성
            CardTransaction transaction = CardTransaction.builder()
                    .transactionId(transactionId)
                    .card(card)
                    .tranDate(request.getTranDate() != null ? request.getTranDate() : LocalDate.now())
                    .tranTime(request.getTranTime() != null ? request.getTranTime() : LocalTime.now())
                    .merchantName(request.getMerchantName())
                    .merchantRegno(request.getMerchantRegno())
                    .approvedAmt(request.getAmount())
                    .tranType(request.getTranType() != null ? request.getTranType() : CardTransaction.TransactionType.APPROVAL)
                    .category(request.getCategory() != null ? request.getCategory() : CardTransaction.TransactionCategory.OTHERS)
                    .memo(request.getMemo())
                    .build();

            // 5. 거래 저장
            CardTransaction savedTransaction = cardTransactionRepository.save(transaction);
            log.info("거래 저장 완료 - transactionId: {}, cardNo: {}",
                    savedTransaction.getTransactionId(), request.getCardNo());

            // 6. 청구서에 거래 반영 (별도 서비스 호출로 트랜잭션 분리)
            cardBillTransactionService.addTransactionToBillSafely(savedTransaction);

            log.info("거래 생성 완료 - transactionId: {}, cardNo: {}",
                    savedTransaction.getTransactionId(), request.getCardNo());

            return savedTransaction;

        } catch (BusinessException e) {
            log.error("거래 생성 실패 (비즈니스 오류) - cardNo: {}, error: {}", request.getCardNo(), e.getMessage());
            throw e; // BusinessException은 그대로 재던지기
        } catch (Exception e) {
            log.error("거래 생성 실패 (시스템 오류) - cardNo: {}, error: {}", request.getCardNo(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 청구서 반영을 안전하게 처리 (rollback 에러 방지)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addTransactionToBillSafely(CardTransaction transaction) {
        try {
            cardBillService.addTransactionToBill(transaction);
            log.info("청구서 반영 성공 - transactionId: {}", transaction.getTransactionId());
        } catch (Exception e) {
            log.error("청구서 반영 실패 - transactionId: {}, error: {}", 
                    transaction.getTransactionId(), e.getMessage(), e);
            // 청구서 반영 실패해도 거래 저장은 성공으로 처리 (별도 트랜잭션이므로 롤백되지 않음)
        }
    }
    
    /**
     * 거래 ID 생성
     */
    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * cardNo로 거래 내역 조회
     */
    @Transactional(readOnly = true)
    public java.util.List<CardTransaction> getTransactionsByCardNo(String cardNo) {
        log.info("거래 내역 조회 - cardNo: {}", cardNo);
        
        try {
            Card card = cardRepository.findByCardNo(cardNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            
            return cardTransactionRepository.findByCard(card);
        } catch (Exception e) {
            log.error("거래 내역 조회 실패 - cardNo: {}, error: {}", cardNo, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * cardNo와 기간으로 거래 내역 조회
     */
    @Transactional(readOnly = true)
    public java.util.List<CardTransaction> getTransactionsByCardNoAndDateRange(
            String cardNo, LocalDate fromDate, LocalDate toDate) {
        log.info("기간별 거래 내역 조회 - cardNo: {}, fromDate: {}, toDate: {}", cardNo, fromDate, toDate);
        
        try {
            Card card = cardRepository.findByCardNo(cardNo)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
            
            return cardTransactionRepository.findByCardAndDateRange(card, fromDate, toDate);
        } catch (Exception e) {
            log.error("기간별 거래 내역 조회 실패 - cardNo: {}, error: {}", cardNo, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 거래 생성 요청 DTO
     */
    public static class CreateTransactionRequest {
        private String cardNo;
        private BigDecimal amount;
        private String merchantName;
        private String merchantRegno;
        private LocalDate tranDate;
        private LocalTime tranTime;
        private CardTransaction.TransactionType tranType;
        private CardTransaction.TransactionCategory category;
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
        
        public CardTransaction.TransactionType getTranType() { return tranType; }
        public void setTranType(CardTransaction.TransactionType tranType) { this.tranType = tranType; }
        
        public CardTransaction.TransactionCategory getCategory() { return category; }
        public void setCategory(CardTransaction.TransactionCategory category) { this.category = category; }
        
        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
        
        @Override
        public String toString() {
            return "CreateTransactionRequest{" +
                    "cardNo='" + cardNo + '\'' +
                    ", amount=" + amount +
                    ", merchantName='" + merchantName + '\'' +
                    ", tranDate=" + tranDate +
                    ", tranTime=" + tranTime +
                    ", tranType=" + tranType +
                    ", category=" + category +
                    ", memo='" + memo + '\'' +
                    '}';
        }
    }
}