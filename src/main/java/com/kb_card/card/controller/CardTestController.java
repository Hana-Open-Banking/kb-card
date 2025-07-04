package com.kb_card.card.controller;

import com.kb_card.card.service.CardBillScheduler;
import com.kb_card.card.service.CardTransactionService;
import com.kb_card.card.entity.CardTransaction;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/admin/test")
@RequiredArgsConstructor
public class CardTestController {
    
    private final CardBillScheduler cardBillScheduler;
    private final CardTransactionService cardTransactionService;
    
    @PostMapping("/setup/{cardNo}")
    @Operation(summary = "전체 시스템 테스트 셋업", description = "청구서 생성 + 샘플 거래 데이터 생성")
    public String setupTestData(@PathVariable String cardNo) {
        log.info("전체 시스템 테스트 셋업 시작 - cardNo: {}", cardNo);
        
        try {
            // 1. 현재 달 청구서 생성
            String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            cardBillScheduler.createBillsManually(currentMonth);
            log.info("✅ 현재 달 청구서 생성 완료: {}", currentMonth);
            
            // 2. 샘플 거래 데이터 생성
            createSampleTransactions(cardNo);
            log.info("✅ 샘플 거래 데이터 생성 완료");
            
            return String.format("✅ 테스트 셋업 완료!\n" +
                    "📋 생성된 청구서: %s월\n" +
                    "💳 테스트 카드: %s\n" +
                    "🧾 생성된 거래: 5건\n\n" +
                    "다음 API로 테스트하세요:\n" +
                    "• GET /api/admin/transactions/list/%s - 거래 내역 조회\n" +
                    "• 오픈뱅킹 청구서 API - /v2.0/cards/bills", 
                    currentMonth, cardNo, cardNo);
            
        } catch (Exception e) {
            log.error("테스트 셋업 실패", e);
            return "❌ 테스트 셋업 실패: " + e.getMessage();
        }
    }
    
    @PostMapping("/transaction/{cardNo}")
    @Operation(summary = "즉시 거래 생성", description = "즉시 새로운 거래를 생성합니다")
    public String createInstantTransaction(
            @PathVariable String cardNo,
            @RequestParam BigDecimal amount,
            @RequestParam String merchantName) {
        
        log.info("즉시 거래 생성 - cardNo: {}, amount: {}, merchantName: {}", cardNo, amount, merchantName);
        
        try {
            CardTransactionService.CreateTransactionRequest request = 
                    new CardTransactionService.CreateTransactionRequest();
            request.setCardNo(cardNo);
            request.setAmount(amount);
            request.setMerchantName(merchantName);
            request.setTranDate(LocalDate.now());
            request.setTranTime(LocalTime.now());
            request.setTranType(CardTransaction.TransactionType.APPROVAL);
            request.setCategory(CardTransaction.TransactionCategory.OTHERS);
            request.setMemo("즉시 생성 테스트");
            
            CardTransaction transaction = cardTransactionService.createTransaction(request);
            
            return String.format("✅ 거래 생성 완료!\n" +
                    "🆔 거래ID: %s\n" +
                    "💳 카드번호: %s\n" +
                    "💰 금액: %s원\n" +
                    "🏪 가맹점: %s\n" +
                    "⏰ 시간: %s %s\n\n" +
                    "청구서에 자동 반영되었습니다!", 
                    transaction.getTransactionId(),
                    transaction.getCardNo(),
                    transaction.getApprovedAmt(),
                    transaction.getMerchantName(),
                    transaction.getTranDate(),
                    transaction.getTranTime());
            
        } catch (Exception e) {
            log.error("즉시 거래 생성 실패", e);
            return "❌ 거래 생성 실패: " + e.getMessage();
        }
    }
    
    @PostMapping("/bills/create/{targetMonth}")
    @Operation(summary = "특정 월 청구서 생성", description = "지정된 월의 청구서를 수동으로 생성합니다")
    public String createMonthlyBills(@PathVariable String targetMonth) {
        log.info("특정 월 청구서 생성 - targetMonth: {}", targetMonth);
        
        try {
            cardBillScheduler.createBillsManually(targetMonth);
            return String.format("✅ %s월 청구서 생성 완료!", targetMonth);
        } catch (Exception e) {
            log.error("청구서 생성 실패", e);
            return "❌ 청구서 생성 실패: " + e.getMessage();
        }
    }
    
    @PostMapping("/bills/close/{targetMonth}")
    @Operation(summary = "특정 월 청구서 확정", description = "지정된 월의 청구서를 확정 처리합니다")
    public String closeMonthlyBills(@PathVariable String targetMonth) {
        log.info("특정 월 청구서 확정 - targetMonth: {}", targetMonth);
        
        try {
            cardBillScheduler.closeBillsManually(targetMonth);
            return String.format("✅ %s월 청구서 확정 완료!", targetMonth);
        } catch (Exception e) {
            log.error("청구서 확정 실패", e);
            return "❌ 청구서 확정 실패: " + e.getMessage();
        }
    }
    
    @GetMapping("/status/{cardNo}")
    @Operation(summary = "카드 상태 조회", description = "카드의 현재 상태와 청구서 정보를 조회합니다")
    public String getCardStatus(@PathVariable String cardNo) {
        log.info("카드 상태 조회 - cardNo: {}", cardNo);
        
        try {
            // 거래 내역 조회
            var transactions = cardTransactionService.getTransactionsByCardNo(cardNo);
            
            return String.format("📊 카드 상태 정보\n" +
                    "💳 카드번호: %s\n" +
                    "📝 총 거래 건수: %d건\n" +
                    "💰 총 거래 금액: %s원\n\n" +
                    "최근 거래:\n%s", 
                    cardNo,
                    transactions.size(),
                    transactions.stream()
                            .map(t -> t.getApprovedAmt())
                            .reduce(BigDecimal.ZERO, BigDecimal::add),
                    transactions.stream()
                            .limit(3)
                            .map(t -> String.format("• %s %s원 (%s)", 
                                    t.getMerchantName(), t.getApprovedAmt(), t.getTranDate()))
                            .reduce("", (a, b) -> a + b + "\n"));
            
        } catch (Exception e) {
            log.error("카드 상태 조회 실패", e);
            return "❌ 카드 상태 조회 실패: " + e.getMessage();
        }
    }
    
    /**
     * 샘플 거래 데이터 생성
     */
    private void createSampleTransactions(String cardNo) {
        // 주유소 거래
        createSampleTransaction(cardNo, new BigDecimal("50000"), "SK주유소 강남점", "FUEL");
        
        // 스타벅스 거래
        createSampleTransaction(cardNo, new BigDecimal("4500"), "스타벅스 역삼점", "FOOD");
        
        // 마트 거래
        createSampleTransaction(cardNo, new BigDecimal("32000"), "이마트 강남점", "SHOPPING");
        
        // 통행료 거래
        createSampleTransaction(cardNo, new BigDecimal("3000"), "한국도로공사 경부고속도로", "TOLL");
        
        // 주차료 거래
        createSampleTransaction(cardNo, new BigDecimal("2000"), "코엑스 주차장", "PARKING");
    }
    
    /**
     * 단일 샘플 거래 생성
     */
    private void createSampleTransaction(String cardNo, BigDecimal amount, String merchantName, String category) {
        try {
            CardTransactionService.CreateTransactionRequest request = 
                    new CardTransactionService.CreateTransactionRequest();
            request.setCardNo(cardNo);
            request.setAmount(amount);
            request.setMerchantName(merchantName);
            request.setTranDate(LocalDate.now().minusDays((int)(Math.random() * 15))); // 최근 15일 내 랜덤
            request.setTranTime(LocalTime.now().minusHours((int)(Math.random() * 24))); // 24시간 내 랜덤
            request.setTranType(CardTransaction.TransactionType.APPROVAL);
            request.setCategory(CardTransaction.TransactionCategory.valueOf(category));
            request.setMemo("샘플 데이터");
            
            cardTransactionService.createTransaction(request);
            log.info("샘플 거래 생성 완료 - merchantName: {}, amount: {}", merchantName, amount);
            
        } catch (Exception e) {
            log.error("샘플 거래 생성 실패 - merchantName: {}", merchantName, e);
        }
    }
} 