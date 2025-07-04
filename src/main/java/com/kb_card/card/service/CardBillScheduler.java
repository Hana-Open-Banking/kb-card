package com.kb_card.card.service;

import com.kb_card.card.entity.Card;
import com.kb_card.card.entity.CardBill;
import com.kb_card.card.entity.CardUser;
import com.kb_card.card.repository.CardBillRepository;
import com.kb_card.card.repository.CardRepository;
import com.kb_card.card.repository.CardUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardBillScheduler {
    
    private final CardBillRepository cardBillRepository;
    private final CardRepository cardRepository;
    private final CardUserRepository cardUserRepository;
    
    /**
     * 매월 1일 00:00에 새로운 청구서 생성
     * 예: 7/1에 7월 청구서 생성 (7/1~7/31 사용분이 기록될 청구서)
     */
    @Scheduled(cron = "0 0 0 1 * *") // 매월 1일 자정
    @Transactional
    public void createMonthlyBills() {
        log.info("=== 매월 청구서 생성 스케줄러 시작 ===");
        
        try {
            String currentMonth = getCurrentMonth(); // 예: 202407
            log.info("{}월 청구서 생성 시작", currentMonth);
            
            // 모든 활성 사용자의 유효한 카드 조회
            List<Card> activeCards = cardRepository.findAll().stream()
                    .filter(card -> card.getCardUser().getStatus() == CardUser.UserStatus.ACTIVE)
                    .filter(Card::isValidCard)
                    .toList();
            
            log.info("청구서 생성 대상 카드 수: {}", activeCards.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (Card card : activeCards) {
                try {
                    // 이미 해당 월의 청구서가 있는지 확인
                    boolean exists = cardBillRepository.findByCardAndChargeMonth(card, currentMonth).isPresent();
                    
                    if (!exists) {
                        // 새로운 청구서 생성
                        CardBill newBill = createNewBill(card, currentMonth);
                        cardBillRepository.save(newBill);
                        
                        log.debug("청구서 생성 완료 - cardNo: {}, chargeMonth: {}", 
                                card.getCardNo(), currentMonth);
                        successCount++;
                    } else {
                        log.debug("이미 존재하는 청구서 - cardNo: {}, chargeMonth: {}", 
                                card.getCardNo(), currentMonth);
                    }
                    
                } catch (Exception e) {
                    log.error("청구서 생성 실패 - cardNo: {}, error: {}", 
                            card.getCardNo(), e.getMessage(), e);
                    failCount++;
                }
            }
            
            log.info("{}월 청구서 생성 완료 - 성공: {}, 실패: {}", currentMonth, successCount, failCount);
            
        } catch (Exception e) {
            log.error("매월 청구서 생성 스케줄러 실행 중 오류 발생", e);
        }
        
        log.info("=== 매월 청구서 생성 스케줄러 종료 ===");
    }
    
    /**
     * 매월 1일 01:00에 이전 달 청구서 확정
     * 예: 7/1에 6월 청구서 확정 (더 이상 변경되지 않음)
     */
    @Scheduled(cron = "0 0 1 1 * *") // 매월 1일 01:00
    @Transactional
    public void closePreviousMonthBills() {
        log.info("=== 이전 달 청구서 확정 스케줄러 시작 ===");
        
        try {
            String previousMonth = getPreviousMonth(); // 예: 202406
            log.info("{}월 청구서 확정 시작", previousMonth);
            
            // 이전 달의 활성 상태 청구서들 조회
            List<CardBill> billsToClose = cardBillRepository.findAll().stream()
                    .filter(bill -> bill.getChargeMonth().equals(previousMonth))
                    .filter(CardBill::isActive)
                    .toList();
            
            log.info("확정 대상 청구서 수: {}", billsToClose.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (CardBill bill : billsToClose) {
                try {
                    // 청구서 총액 최종 계산
                    bill.updateTotalAmount();
                    
                    // 청구서 확정 처리
                    bill.close();
                    
                    cardBillRepository.save(bill);
                    
                    log.debug("청구서 확정 완료 - cardNo: {}, chargeMonth: {}, totalAmount: {}", 
                            bill.getCardNo(), bill.getChargeMonth(), bill.getChargeAmt());
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("청구서 확정 실패 - cardNo: {}, chargeMonth: {}, error: {}", 
                            bill.getCardNo(), bill.getChargeMonth(), e.getMessage(), e);
                    failCount++;
                }
            }
            
            log.info("{}월 청구서 확정 완료 - 성공: {}, 실패: {}", previousMonth, successCount, failCount);
            
        } catch (Exception e) {
            log.error("이전 달 청구서 확정 스케줄러 실행 중 오류 발생", e);
        }
        
        log.info("=== 이전 달 청구서 확정 스케줄러 종료 ===");
    }
    
    /**
     * 새로운 청구서 생성
     */
    private CardBill createNewBill(Card card, String chargeMonth) {
        // 결제일 계산 (기본값: 25일)
        String settlementDay = "25";
        String settlementDate = calculateSettlementDate(chargeMonth, settlementDay);
        
        // 신용/체크 구분 결정
        String creditCheckType = determineCreditCheckType(card);
        
        return CardBill.builder()
                .card(card)
                .chargeMonth(chargeMonth)
                .settlementSeqNo("0001") // 기본값
                .chargeAmt(java.math.BigDecimal.ZERO) // 초기값 0
                .settlementDay(settlementDay)
                .settlementDate(settlementDate)
                .creditCheckType(creditCheckType)
                .billStatus(CardBill.BillStatus.ACTIVE)
                .build();
    }
    
    /**
     * 현재 월 조회 (YYYYMM)
     */
    private String getCurrentMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    }
    
    /**
     * 이전 월 조회 (YYYYMM)
     */
    private String getPreviousMonth() {
        return LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
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
                dayOfMonth = lastDayOfMonth; // 해당 월의 마지막 날로 조정
            }
            
            LocalDate settlementDate = LocalDate.of(nextMonth.getYear(), nextMonth.getMonth(), dayOfMonth);
            
            // 휴일 처리 (간단한 예시: 토요일이면 월요일로, 일요일이면 월요일로)
            while (settlementDate.getDayOfWeek().getValue() >= 6) { // 토요일(6) 또는 일요일(7)
                settlementDate = settlementDate.plusDays(1);
            }
            
            return settlementDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
        } catch (Exception e) {
            log.error("결제 예정일 계산 실패 - chargeMonth: {}, settlementDay: {}", chargeMonth, settlementDay, e);
            // 기본값 반환
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
                default -> "01"; // 기본값: 신용
            };
        }
        return "01"; // 기본값: 신용
    }
    
    /**
     * 수동 청구서 생성 (테스트용)
     */
    @Transactional
    public void createBillsManually(String targetMonth) {
        log.info("수동 청구서 생성 시작 - targetMonth: {}", targetMonth);
        
        List<Card> activeCards = cardRepository.findAll().stream()
                .filter(card -> card.getCardUser().getStatus() == CardUser.UserStatus.ACTIVE)
                .filter(Card::isValidCard)
                .toList();
        
        for (Card card : activeCards) {
            boolean exists = cardBillRepository.findByCardAndChargeMonth(card, targetMonth).isPresent();
            
            if (!exists) {
                CardBill newBill = createNewBill(card, targetMonth);
                cardBillRepository.save(newBill);
                log.info("수동 청구서 생성 완료 - cardNo: {}, chargeMonth: {}", 
                        card.getCardNo(), targetMonth);
            }
        }
        
        log.info("수동 청구서 생성 완료 - targetMonth: {}", targetMonth);
    }
    
    /**
     * 수동 청구서 확정 (테스트용)
     */
    @Transactional
    public void closeBillsManually(String targetMonth) {
        log.info("수동 청구서 확정 시작 - targetMonth: {}", targetMonth);
        
        List<CardBill> billsToClose = cardBillRepository.findAll().stream()
                .filter(bill -> bill.getChargeMonth().equals(targetMonth))
                .filter(CardBill::isActive)
                .toList();
        
        for (CardBill bill : billsToClose) {
            bill.updateTotalAmount();
            bill.close();
            cardBillRepository.save(bill);
            log.info("수동 청구서 확정 완료 - cardNo: {}, chargeMonth: {}, totalAmount: {}", 
                    bill.getCardNo(), bill.getChargeMonth(), bill.getChargeAmt());
        }
        
        log.info("수동 청구서 확정 완료 - targetMonth: {}", targetMonth);
    }
}