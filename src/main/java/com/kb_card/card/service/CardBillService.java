package com.kb_card.card.service;

import com.kb_card.card.entity.Card;
import com.kb_card.card.entity.CardBill;
import com.kb_card.card.entity.CardBillDetail;
import com.kb_card.card.entity.CardTransaction;
import com.kb_card.card.repository.CardBillDetailRepository;
import com.kb_card.card.repository.CardBillRepository;
import com.kb_card.card.repository.CardRepository;
import com.kb_card.common.exception.BusinessException;
import com.kb_card.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardBillService {
    
    private final CardBillRepository cardBillRepository;
    private final CardBillDetailRepository cardBillDetailRepository;
    private final CardRepository cardRepository;
    
    /**
     * 거래 발생 시 청구서에 상세 내역 추가
     */
    @Transactional
    public void addTransactionToBill(CardTransaction transaction) {
        log.info("거래 청구서 반영 시작 - transactionId: {}, cardNo: {}, amount: {}", 
                transaction.getTransactionId(), transaction.getCardNo(), transaction.getApprovedAmt());
        
        try {
            // 1. 카드 조회
            Card card = transaction.getCard();
            if (card == null) {
                log.error("카드 정보를 찾을 수 없음 - transactionId: {}", transaction.getTransactionId());
                throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
            }
            
            // 2. 현재 달 활성 청구서 조회 또는 생성
            String currentMonth = getCurrentMonth();
            CardBill activeBill = getOrCreateActiveBill(card, currentMonth);
            
            // 3. 청구서 상세 내역 생성
            CardBillDetail billDetail = createBillDetailFromTransaction(activeBill, transaction);
            
            // 4. 청구서 상세 내역 저장
            cardBillDetailRepository.save(billDetail);
            
            // 5. 청구서 총액 업데이트
            activeBill.addBillDetail(billDetail);
            cardBillRepository.save(activeBill);
            
            log.info("거래 청구서 반영 완료 - transactionId: {}, cardNo: {}, 현재 청구서 총액: {}", 
                    transaction.getTransactionId(), transaction.getCardNo(), activeBill.getChargeAmt());
            
        } catch (Exception e) {
            log.error("거래 청구서 반영 실패 - transactionId: {}, error: {}", 
                    transaction.getTransactionId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 현재 달 활성 청구서 조회 또는 생성
     */
    private CardBill getOrCreateActiveBill(Card card, String currentMonth) {
        // 기존 활성 청구서 조회
        Optional<CardBill> existingBill = cardBillRepository.findActiveCardBill(card);
        
        if (existingBill.isPresent()) {
            CardBill bill = existingBill.get();
            // 청구서가 현재 달인지 확인
            if (bill.getChargeMonth().equals(currentMonth)) {
                return bill;
            } else {
                // 다른 달 청구서면 새로 생성
                log.warn("활성 청구서가 현재 달이 아님 - cardNo: {}, billMonth: {}, currentMonth: {}", 
                        card.getCardNo(), bill.getChargeMonth(), currentMonth);
            }
        }
        
        // 새로운 청구서 생성
        return createNewBill(card, currentMonth);
    }
    
    /**
     * 새로운 청구서 생성
     */
    private CardBill createNewBill(Card card, String chargeMonth) {
        log.info("새로운 청구서 생성 - cardNo: {}, chargeMonth: {}", card.getCardNo(), chargeMonth);
        
        // 결제일 계산 (기본값: 25일)
        String settlementDay = "25";
        String settlementDate = calculateSettlementDate(chargeMonth, settlementDay);
        
        // 신용/체크 구분 결정
        String creditCheckType = determineCreditCheckType(card);
        
        CardBill newBill = CardBill.builder()
                .card(card)
                .chargeMonth(chargeMonth)
                .settlementSeqNo("0001")
                .chargeAmt(BigDecimal.ZERO)
                .settlementDay(settlementDay)
                .settlementDate(settlementDate)
                .creditCheckType(creditCheckType)
                .billStatus(CardBill.BillStatus.ACTIVE)
                .build();
        
        return cardBillRepository.save(newBill);
    }
    
    /**
     * 거래 내역을 청구서 상세 내역으로 변환
     */
    private CardBillDetail createBillDetailFromTransaction(CardBill cardBill, CardTransaction transaction) {
        // 가맹점명 마스킹 처리
        String maskedMerchantName = maskMerchantName(transaction.getMerchantName());
        
        // 상품 구분 결정
        String productType = determineProductType(transaction);
        
        // 거래 시간 포맷 (HHmmss)
        String paidTime = transaction.getTranTime().format(DateTimeFormatter.ofPattern("HHmmss"));
        
        // 거래 날짜 포맷 (yyyyMMdd)
        String paidDate = transaction.getTranDate().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        return CardBillDetail.builder()
                .cardBill(cardBill)
                .cardId(String.valueOf(cardBill.getCard().getId())) // 카드 ID
                .paidDate(paidDate)
                .paidTime(paidTime)
                .paidAmt(transaction.getApprovedAmt())
                .merchantNameMasked(maskedMerchantName)
                .creditFeeAmt(BigDecimal.ZERO) // 기본값, 필요 시 수수료 계산 로직 추가
                .productType(productType)
                .build();
    }
    
    /**
     * 가맹점명 마스킹 처리
     */
    private String maskMerchantName(String merchantName) {
        if (merchantName == null || merchantName.length() <= 2) {
            return merchantName;
        }
        
        // 2글자 이상일 때 뒤의 일부를 마스킹
        if (merchantName.length() <= 4) {
            return merchantName.substring(0, 2) + "**";
        } else {
            return merchantName.substring(0, 3) + "**";
        }
    }
    
    /**
     * 상품 구분 결정
     */
    private String determineProductType(CardTransaction transaction) {
        // 거래 유형에 따라 상품 구분 결정
        return switch (transaction.getTranType()) {
            case APPROVAL -> "01"; // 일시불
            case CANCEL -> "01";   // 취소도 일시불로 처리
            default -> "01";       // 기본값: 일시불
        };
    }
    
    /**
     * 현재 월 조회 (YYYYMM)
     */
    private String getCurrentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
    
    /**
     * 결제 예정일 계산 (YYYYMMDD)
     */
    private String calculateSettlementDate(String chargeMonth, String settlementDay) {
        try {
            // 다음 달 결제일 계산
            LocalDate chargeDate = LocalDate.parse(chargeMonth + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate nextMonth = chargeDate.plusMonths(1);
            
            int dayOfMonth = Integer.parseInt(settlementDay);
            
            // 해당 월의 마지막 날짜 확인
            int lastDayOfMonth = nextMonth.lengthOfMonth();
            if (dayOfMonth > lastDayOfMonth) {
                dayOfMonth = lastDayOfMonth;
            }
            
            LocalDate settlementDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), dayOfMonth);
            
            // 휴일 처리 (간단한 예시: 주말이면 다음 평일로)
            while (settlementDate.getDayOfWeek().getValue() >= 6) {
                settlementDate = settlementDate.plusDays(1);
            }
            
            return settlementDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
        } catch (Exception e) {
            log.error("결제 예정일 계산 실패", e);
            return LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
    }
    
    /**
     * 신용/체크 구분 결정
     */
    private String determineCreditCheckType(Card card) {
        if (card.getCardProduct() != null) {
            return switch (card.getCardProduct().getCardType()) {
                case CREDIT -> "01"; // 신용
                case DEBIT -> "02";  // 체크
                case PREPAID -> "02"; // 선불은 체크로 분류
                default -> "01";     // 기본값: 신용
            };
        }
        return "01"; // 기본값: 신용
    }
    
    /**
     * 청구서 총액 재계산 (필요 시 사용)
     */
    @Transactional
    public void recalculateBillAmount(Long billId) {
        log.info("청구서 총액 재계산 시작 - billId: {}", billId);
        
        CardBill bill = cardBillRepository.findById(billId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
        
        // 상세 내역 기준으로 총액 재계산
        bill.updateTotalAmount();
        cardBillRepository.save(bill);
        
        log.info("청구서 총액 재계산 완료 - billId: {}, totalAmount: {}", billId, bill.getChargeAmt());
    }
    
    /**
     * 특정 카드의 활성 청구서 조회
     */
    @Transactional(readOnly = true)
    public Optional<CardBill> getActiveBill(Card card) {
        return cardBillRepository.findActiveCardBill(card);
    }
    
    /**
     * 특정 카드의 현재 달 청구서 조회
     */
    @Transactional(readOnly = true)
    public Optional<CardBill> getCurrentMonthBill(Card card) {
        String currentMonth = getCurrentMonth();
        return cardBillRepository.findByCardAndChargeMonth(card, currentMonth);
    }
}