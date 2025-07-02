package com.kb_card.card.controller;

import com.kb_card.card.dto.*;
import com.kb_card.card.service.CardUserService;
import com.kb_card.common.exception.BusinessException;
import com.kb_card.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v2.0")
@RequiredArgsConstructor
@Tag(name = "KB카드 서비스", description = "KB카드 발급 및 정보 제공 API")
public class CardUserController {
    
    private final CardUserService cardUserService;

    // ========== 카드사 고유 서비스 ==========
    
    @Operation(summary = "사용자 탈퇴", 
               description = "카드사용자 탈퇴 처리")
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawUser(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String userCi) {
        
        log.info("사용자 탈퇴 요청 - userCi: {}", userCi);
        
        // Authorization 헤더 검증 (Bearer 토큰)
        validateAuthorization(authorization);
        
        cardUserService.withdrawUser(userCi);
        
        return ResponseEntity.ok("탈퇴 처리 완료");
    }
    
    @Operation(summary = "새로운 카드 발급 신청", 
               description = "새로운 카드 발급을 신청합니다.")
    @PostMapping("/cards/issue")
    public ResponseEntity<CardIssueResponse> issueNewCard(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CardIssueRequest request) {
        
        log.info("새로운 카드 발급 신청 - userCi: {}, cardType: {}, cardProductName: {}", 
                request.getUserCi(), request.getCardType(), request.getCardProductName());
        
        validateAuthorization(authorization);
        
        CardIssueResponse response = cardUserService.issueNewCard(request);
        
        log.info("새로운 카드 발급 신청 응답 - applicationId: {}, issueStatus: {}", 
                response.getApplicationId(), response.getIssueStatus());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "카드 발급 신청 정보 (JSON)", 
               description = "카드 발급 신청을 위한 폼 정보를 JSON으로 제공합니다.")
    @GetMapping("/cards/issue-form")
    public ResponseEntity<Map<String, Object>> getCardIssueForm(@RequestParam(required = false) String returnUrl) {
        
        log.info("카드 발급 신청 정보 요청");
        
        Map<String, Object> formInfo = new HashMap<>();
        
        // 폼 기본 정보
        formInfo.put("title", "KB카드 발급 신청");
        formInfo.put("description", "간편하고 빠른 카드 발급 서비스");
        formInfo.put("submitUrl", "/v2.0/cards/issue");
        formInfo.put("method", "POST");
        
        // 카드 종류 옵션
        List<Map<String, Object>> cardTypes = List.of(
            Map.of(
                "type", "CREDIT",
                "name", "신용카드",
                "icon", "💎",
                "description", "할부 및 다양한 혜택 제공",
                "benefits", List.of("포인트 적립", "할부 결제", "다양한 혜택")
            ),
            Map.of(
                "type", "CHECK", 
                "name", "체크카드",
                "icon", "💰",
                "description", "계좌 잔액 내에서 즉시 결제",
                "benefits", List.of("실시간 출금", "가계부 관리", "연회비 절약")
            )
        );
        formInfo.put("cardTypes", cardTypes);
        
        // 카드 상품 목록
        List<Map<String, Object>> cardProducts = List.of(
            Map.of(
                "code", "KB_ONE_CARD",
                "name", "KB국민 원카드",
                "description", "하나로 통합된 올인원 카드",
                "annualFee", 0,
                "benefits", List.of("대중교통 할인", "편의점 할인", "카페 할인")
            ),
            Map.of(
                "code", "KB_HANA_CARD",
                "name", "KB국민 하나카드", 
                "description", "하나금융그룹 제휴 카드",
                "annualFee", 15000,
                "benefits", List.of("하나은행 우대", "ATM 수수료 면제", "포인트 적립")
            ),
            Map.of(
                "code", "KB_TALK_CARD",
                "name", "KB국민 톡톡카드",
                "description", "소통하는 젊은 세대를 위한 카드",
                "annualFee", 10000,
                "benefits", List.of("온라인 쇼핑 할인", "배달앱 할인", "OTT 할인")
            ),
            Map.of(
                "code", "KB_CAR_PREMIUM",
                "name", "KB국민 자동차 프리미엄카드",
                "description", "자동차 관련 최고 혜택 카드",
                "annualFee", 150000,
                "benefits", List.of("주유 8% 할인", "정비비 5% 할인", "톨게이트 무료")
            ),
            Map.of(
                "code", "KB_CAR_LITE",
                "name", "KB국민 자동차 라이트카드",
                "description", "부담 없는 자동차 카드",
                "annualFee", 0,
                "benefits", List.of("주유 3% 할인", "주차비 할인", "세차 할인")
            )
        );
        formInfo.put("cardProducts", cardProducts);
        
        // 폼 필드 정의
        List<Map<String, Object>> formFields = List.of(
            Map.of(
                "name", "userName",
                "label", "이름",
                "type", "text",
                "required", true,
                "placeholder", "홍길동"
            ),
            Map.of(
                "name", "userCi",
                "label", "고객 CI",
                "type", "text",
                "required", true,
                "placeholder", "고객 식별 정보"
            ),
            Map.of(
                "name", "cardType",
                "label", "카드 종류",
                "type", "select",
                "required", true,
                "options", List.of(
                    Map.of("value", "CREDIT", "label", "신용카드"),
                    Map.of("value", "CHECK", "label", "체크카드")
                )
            ),
            Map.of(
                "name", "cardProductName",
                "label", "카드 상품",
                "type", "select",
                "required", true,
                "description", "원하시는 카드 상품을 선택해주세요"
            ),
            Map.of(
                "name", "deliveryAddress",
                "label", "배송 주소",
                "type", "textarea",
                "required", true,
                "placeholder", "카드를 받으실 주소를 입력해주세요"
            )
        );
        formInfo.put("formFields", formFields);
        
        // 추가 정보
        formInfo.put("estimatedProcessingTime", "영업일 기준 3-5일");
        formInfo.put("deliveryInfo", "본인 확인 후 등기 배송");
        
        if (returnUrl != null) {
            formInfo.put("returnUrl", returnUrl);
        }
        
        return ResponseEntity.ok(formInfo);
    }

    // ========== KFTC 요청 처리용 내부 API ==========
    
    @PostMapping("/cards/list")
    @Operation(
        summary = "카드목록조회 API (내부)",
        description = "KFTC에서 호출하는 카드 목록 조회"
    )
    public ResponseEntity<CardListResponse> getCardList(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CardListRequest request) {
        
        log.info("카드목록조회 API 호출 - bankTranId: {}, userCi: {}", 
                request.getBankTranId(), request.getUserCi());
        
        validateAuthorization(authorization);
        
        CardListResponse response = cardUserService.getCardList(request);
        
        log.info("카드목록조회 API 응답 - cardCnt: {}, rspCode: {}", 
                response.getCardCnt(), response.getRspCode());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "카드조회해지 (내부)", 
               description = "KFTC에서 호출하는 카드조회해지 처리")
    @PostMapping("/cards/cancel")
    public ResponseEntity<CardCancelResponse> cancelCardInquiry(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody CardCancelRequest request) {
        
        log.info("카드조회해지 API 호출 - bankTranId: {}, userSeqNo: {}", 
                request.getBankTranId(), request.getUserSeqNo());
        
        validateAuthorization(authorization);
        
        CardCancelResponse response = cardUserService.cancelCardInquiry(request);
        
        log.info("카드조회해지 API 응답 - bankTranId: {}, rspCode: {}", 
                request.getBankTranId(), response.getRspCode());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cards/issue_info")
    @Operation(
        summary = "카드기본정보조회 API (내부)",
        description = "KFTC에서 호출하는 카드 기본정보 조회"
    )
    public ResponseEntity<CardIssueInfoResponse> getCardIssueInfo(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CardIssueInfoRequest request) {
        
        log.info("카드기본정보조회 API 호출 - bankTranId: {}, cardId: {}", 
                request.getBankTranId(), request.getCardId());
        
        validateAuthorization(authorization);
        
        CardIssueInfoResponse response = cardUserService.getCardIssueInfo(request);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cards/bills")
    @Operation(
        summary = "카드청구기본정보조회 API (내부)",
        description = "KFTC에서 호출하는 카드 청구 기본정보 조회"
    )
    public ResponseEntity<CardBillsResponse> getCardBills(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CardBillsRequest request) {
        
        log.info("카드청구기본정보조회 API 호출 - bankTranId: {}, fromMonth: {}, toMonth: {}", 
                request.getBankTranId(), request.getFromMonth(), request.getToMonth());
        
        validateAuthorization(authorization);
        
        CardBillsResponse response = cardUserService.getCardBills(request);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cards/bills/detail")
    @Operation(
        summary = "카드청구상세정보조회 API (내부)",
        description = "KFTC에서 호출하는 카드 청구 상세정보 조회"
    )
    public ResponseEntity<CardBillDetailResponse> getCardBillDetail(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CardBillDetailRequest request) {
        
        log.info("카드청구상세정보조회 API 호출 - bankTranId: {}, chargeMonth: {}, settlementSeqNo: {}", 
                request.getBankTranId(), request.getChargeMonth(), request.getSettlementSeqNo());
        
        validateAuthorization(authorization);
        
        CardBillDetailResponse response = cardUserService.getCardBillDetail(request);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cards/transactions")
    @Operation(
        summary = "카드거래내역조회 API (내부)",
        description = "KFTC에서 호출하는 카드 거래내역 조회"
    )
    public ResponseEntity<CardTransactionResponse> getCardTransactions(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CardTransactionRequest request) {
        
        log.info("카드거래내역조회 API 호출 - bankTranId: {}, cardId: {}, fromDate: {}, toDate: {}", 
                request.getBankTranId(), request.getCardId(), request.getFromDate(), request.getToDate());
        
        validateAuthorization(authorization);
        
        CardTransactionResponse response = cardUserService.getCardTransactions(request);
        
        log.info("카드거래내역조회 응답 생성 완료 - response: {}", response);
        log.info("거래내역조회 응답 JSON 확인 - rspCode: {}, tranCnt: {}, tranList size: {}", 
                response.getRspCode(), response.getTranCnt(), 
                response.getTranList() != null ? response.getTranList().size() : 0);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Authorization 헤더 검증
     */
    private void validateAuthorization(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.error("유효하지 않은 Authorization 헤더: {}", authorization);
            throw new BusinessException(ErrorCode.INVALID_AUTHORIZATION);
        }
        
        String token = authorization.substring(7);
        if (token.trim().isEmpty()) {
            log.error("Authorization 토큰이 비어있음");
            throw new BusinessException(ErrorCode.INVALID_AUTHORIZATION);
        }
        
        // 실제 운영환경에서는 JWT 토큰 검증 수행
        log.debug("Authorization 토큰 검증 완료");
    }
} 