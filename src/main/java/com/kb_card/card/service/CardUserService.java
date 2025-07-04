package com.kb_card.card.service;

import com.kb_card.card.dto.*;
import com.kb_card.card.entity.*;
import com.kb_card.card.repository.*;
import com.kb_card.common.exception.BusinessException;
import com.kb_card.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardUserService {

    private final CardUserRepository cardUserRepository;
    private final CardRepository cardRepository;
    private final CardApplicationRepository cardApplicationRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final CardBillDetailRepository cardBillDetailRepository;
    private final CardBillRepository cardBillRepository;

    private static final String BANK_NAME = "KB카드";
    private static final String BANK_CODE = "381"; // KB카드 표준코드

    // ========== 카드사 고유 서비스 ==========

    /**
     * 사용자 탈퇴 처리
     */
    @Transactional
    public void withdrawUser(String userCi) {
        log.info("사용자 탈퇴 처리 시작 - userCi: {}", userCi);

        Optional<CardUser> optionalUser = cardUserRepository.findByUserCi(userCi);

        if (optionalUser.isPresent()) {
            CardUser user = optionalUser.get();
            user.withdraw();
            cardUserRepository.save(user);
            log.info("사용자 탈퇴 처리 완료 - userId: {}, userCi: {}", user.getUserId(), userCi);
        } else {
            log.warn("탈퇴할 사용자를 찾을 수 없음 - userCi: {}", userCi);
            throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
        }
    }

    /**
     * 자동 탈퇴 처리 (시스템 배치용)
     * 90일 이상 비활성 사용자 자동 탈퇴
     */
    @Transactional
    public void processAutoWithdrawal() {
        log.info("자동 탈퇴 처리 시작");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        // 실제 구현에서는 Repository에 findInactiveUsers 메서드 추가 필요
        // List<CardUser> inactiveUsers = cardUserRepository.findInactiveUsers(cutoffDate);
        List<CardUser> inactiveUsers = List.of(); // 임시로 빈 리스트

        for (CardUser user : inactiveUsers) {
            user.withdraw();
            cardUserRepository.save(user);
            log.info("자동 탈퇴 처리 - userId: {}", user.getUserId());
        }

        log.info("자동 탈퇴 처리 완료 - 처리 건수: {}", inactiveUsers.size());
    }

    /**
     * 새로운 카드 발급 신청
     */
    @Transactional
    public CardIssueResponse issueNewCard(CardIssueRequest request) {
        log.info("새로운 카드 발급 신청 - userCi: {}, cardType: {}, cardProductName: {}",
                request.getUserCi(), request.getCardType(), request.getCardProductName());

        try {
            // 1. 사용자 조회 또는 생성
            CardUser applicant = cardUserRepository.findByUserCi(request.getUserCi())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

            // 2. 카드 상품 조회 (임시로 기본 상품 생성)
            CardProduct cardProduct = CardProduct.builder()
                    .productCode("KB001")
                    .productName(request.getCardProductName())
                    .cardType("CREDIT".equals(request.getCardType()) ?
                             CardProduct.CardType.CREDIT : CardProduct.CardType.DEBIT)
                    .annualFee(BigDecimal.ZERO)
                    .description("기본 카드 상품")
                    .build();

            // 3. 신청 ID 생성
            String applicationId = generateApplicationId();

            // 4. 기본 심사 (간소화된 버전)
            String issueStatus = performBasicScreening(request);

            // 5. 카드 신청 정보 저장
            CardApplication application = CardApplication.builder()
                    .applicationId(applicationId)
                    .applicant(applicant)
                    .cardProduct(cardProduct)
                    .applicantName("카드신청자") // 실제로는 사용자 정보로부터 가져와야 함
                    .phone(request.getPhoneNumber())
                    .email(request.getEmail() != null ? request.getEmail() : "unknown@example.com")
                    .annualIncome(request.getAnnualIncome() != null ?
                                 BigDecimal.valueOf(request.getAnnualIncome()) : BigDecimal.ZERO)
                    .applicationDate(LocalDateTime.now())
                    .build();

            cardApplicationRepository.save(application);

            // 6. 응답 생성
            return CardIssueResponse.builder()
                    .applicationId(applicationId)
                    .cardProductName(request.getCardProductName())
                    .issueStatus(issueStatus)
                    .message(generateIssueMessage(issueStatus, request.getCardProductName()))
                    .expectedIssueDate(LocalDateTime.now().plusDays(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .expectedDeliveryDate(LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    .build();

        } catch (Exception e) {
            log.error("카드 발급 신청 중 오류 발생 - userCi: {}, error: {}", request.getUserCi(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 신청 ID 생성
     */
    private String generateApplicationId() {
        return "APP" + System.currentTimeMillis() + String.format("%03d", new Random().nextInt(1000));
    }

    /**
     * 기본 심사 수행 (간소화된 버전)
     */
    private String performBasicScreening(CardIssueRequest request) {
        // 실제로는 복합적인 심사 로직이 필요하지만, 샘플에서는 간소화
        Random random = new Random();
        int score = random.nextInt(100);

        if (score >= 70) {
            return "APPROVED"; // 승인
        } else if (score >= 40) {
            return "PENDING"; // 보류 (추가 서류 필요)
        } else {
            return "REJECTED"; // 거절
        }
    }

    /**
     * 발급 상태에 따른 메시지 생성
     */
    private String generateIssueMessage(String issueStatus, String cardProductName) {
        return switch (issueStatus) {
            case "APPROVED" -> cardProductName + " 발급이 승인되었습니다. 5일 내 카드를 받아보실 수 있습니다.";
            case "PENDING" -> "발급 심사가 진행 중입니다. 추가 서류가 필요할 수 있습니다.";
            case "REJECTED" -> "죄송합니다. 현재 카드 발급이 어렵습니다. 고객센터로 문의해주세요.";
            default -> "카드 발급 신청이 접수되었습니다.";
        };
    }

    // ========== KFTC 요청 처리용 내부 API ==========

    /**
     * 카드목록조회 (KFTC 요청 처리)
     */
    @Transactional(readOnly = true)
    public CardListResponse getCardList(CardListRequest request) {
        log.info("카드목록조회 요청 처리 - userCi: {}", request.getUserCi());

        try {
            // 1. 사용자 조회
            log.debug("CardUser 조회 시작 - userCi: {}", request.getUserCi());
            Optional<CardUser> optionalUser = cardUserRepository.findByUserCi(request.getUserCi());

            if (optionalUser.isEmpty()) {
                log.warn("카드목록조회 - 사용자를 찾을 수 없음: {}", request.getUserCi());
                log.warn("DB에서 조회된 사용자 수: {}", cardUserRepository.count());
                return createCardListErrorResponse(request, "555", "해당 사용자 없음");
            }

            CardUser user = optionalUser.get();
            log.info("CardUser 조회 성공 - userId: {}, userName: {}, userCi: {}",
                    user.getUserId(), user.getUserName(), user.getUserCi());

            // 2. 해지된 사용자 확인
            if (user.getStatus() == CardUser.UserStatus.WITHDRAWN) {
                log.warn("카드목록조회 - 해지된 사용자: {}", request.getUserCi());
                return createCardListErrorResponse(request, "551", "기 해지 사용자");
            }

            // 3. 사용자의 카드 목록 조회
            List<Card> userCards = cardRepository.findByUserCi(request.getUserCi());
            log.info("사용자 카드 조회 완료 - userCi: {}, 카드 수: {}", request.getUserCi(), userCards.size());

            // 4. 카드 목록을 응답 형태로 변환
            List<CardListResponse.CardInfo> cardInfoList = userCards.stream()
                    .map(this::convertToCardInfo)
                    .toList();

            log.info("카드목록조회 완료 - userCi: {}, cardCnt: {}",
                    request.getUserCi(), cardInfoList.size());

            return CardListResponse.builder()
                    .apiTranId(generateApiTranId())
                    .apiTranDtm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
                    .rspCode("A0000")
                    .rspMessage("")
                    .bankTranId(request.getBankTranId())
                    .bankTranDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .bankCodeTran(BANK_CODE)
                    .bankRspCode("000")
                    .bankRspMessage("")
                    .nextPageYn("N")
                    .beforInquiryTraceInfo("")
                    .cardList(cardInfoList)
                    .cardCnt(String.valueOf(cardInfoList.size()))
                    .build();

        } catch (Exception e) {
            log.error("카드목록조회 처리 중 오류 발생 - userCi: {}, error: {}",
                     request.getUserCi(), e.getMessage(), e);
            return createCardListErrorResponse(request, "999", "시스템 오류");
        }
    }

    /**
     * Card 엔티티를 CardInfo DTO로 변환
     */
    private CardListResponse.CardInfo convertToCardInfo(Card card) {
        return CardListResponse.CardInfo.builder()
                .cardId(String.valueOf(card.getId())) // Long id를 String으로 변환
                .cardNumMasked(maskCardNumber(card.getCardNo())) // cardNo 필드 사용
                .cardName(card.getCardName())
                .cardMemberType("CREDIT".equals(card.getCardType()) ? "1" : "2") // String 타입으로 비교
                .build();
    }

    /**
     * 카드번호 마스킹 처리
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "****-****-****-****";
        }

        // 16자리 카드번호를 4-4-4-4 형태로 마스킹
        return cardNumber.substring(0, 4) + "-****-****-" + cardNumber.substring(12);
    }

    /**
     * 카드목록조회 에러 응답 생성
     */
    private CardListResponse createCardListErrorResponse(CardListRequest request, String bankRspCode, String bankRspMessage) {
        return CardListResponse.builder()
                .apiTranId(generateApiTranId())
                .apiTranDtm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
                .rspCode("A0001")
                .rspMessage("카드목록조회 실패")
                .bankTranId(request.getBankTranId())
                .bankTranDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .bankCodeTran(BANK_CODE)
                .bankRspCode(bankRspCode)
                .bankRspMessage(bankRspMessage)
                .nextPageYn("N")
                .beforInquiryTraceInfo("")
                .cardList(List.of())
                .cardCnt("0")
                .build();
    }

    /**
     * 카드조회해지 (KFTC 요청 처리)
     */
    @Transactional
    public CardCancelResponse cancelCardInquiry(CardCancelRequest request) {
        log.info("카드조회해지 요청 처리 - userCi: {}", request.getUserCi());

        try {
            Optional<CardUser> optionalUser = cardUserRepository.findByUserCi(request.getUserCi());

            if (optionalUser.isPresent()) {
                CardUser user = optionalUser.get();

                // 이미 해지된 사용자인지 확인
                if (user.getStatus() == CardUser.UserStatus.WITHDRAWN) {
                    log.warn("이미 해지된 사용자 - userCi: {}", request.getUserCi());
                    return createCancelResponse(request, "551", "기 해지 사용자");
                }

                // 사용자 해지 처리
                user.withdraw();
                cardUserRepository.save(user);

                log.info("카드조회해지 완료 - userCi: {}", request.getUserCi());
                return createCancelResponse(request, "000", "");

            } else {
                log.warn("해지할 사용자를 찾을 수 없음 - userCi: {}", request.getUserCi());
                return createCancelResponse(request, "555", "해당 사용자 없음");
            }

        } catch (Exception e) {
            log.error("카드조회해지 처리 중 오류 발생 - userCi: {}, error: {}",
                     request.getUserCi(), e.getMessage(), e);
            return createCancelResponse(request, "999", "시스템 오류");
        }
    }

    /**
     * 카드조회해지 응답 생성
     */
    private CardCancelResponse createCancelResponse(CardCancelRequest request, String bankRspCode, String bankRspMessage) {
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String apiTranId = generateApiTranId();
        String bankTranDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String rspCode = "000".equals(bankRspCode) ? "A0000" : "A0001";

        return CardCancelResponse.builder()
                .apiTranId(apiTranId)
                .apiTranDtm(currentDateTime)
                .rspCode(rspCode)
                .rspMessage("")
                .bankTranId(request.getBankTranId())
                .bankTranDate(bankTranDate)
                .bankCodeTran(BANK_CODE)
                .bankRspCode(bankRspCode)
                .bankRspMessage(bankRspMessage)
                .build();
    }

    /**
     * 카드기본정보조회 (KFTC 요청 처리)
     */
    @Transactional(readOnly = true)
    public CardIssueInfoResponse getCardIssueInfo(CardIssueInfoRequest request) {
        log.info("카드기본정보조회 요청 처리 - cardId: {}, userCi: {}", request.getCardId(), request.getUserCi());

        try {
            // 사용자 CI로 사용자 조회
            Optional<CardUser> optionalUser = cardUserRepository.findByUserCi(request.getUserCi());
            if (optionalUser.isEmpty()) {
                log.warn("사용자를 찾을 수 없음 - userCi: {}", request.getUserCi());
                return createIssueInfoErrorResponse(request, ErrorCode.ENTITY_NOT_FOUND);
            }

            // 실제로는 카드 ID로 카드 정보를 조회해야 하지만, 샘플에서는 고정 응답
            String apiTranId = generateApiTranId();
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String bankTranDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            return CardIssueInfoResponse.builder()
                    .apiTranId(apiTranId)
                    .apiTranDtm(currentDateTime)
                    .rspCode("A0000")
                    .rspMessage("")
                    .bankTranId(request.getBankTranId())
                    .bankTranDate(bankTranDate)
                    .bankCodeTran(BANK_CODE)
                    .bankRspCode("000")
                    .bankRspMessage("")
                    .cardType("01") // 신용카드
                    .settlementBankCode("381") // KB국민은행
                    .settlementAccountNum("0001234567890123")
                    .settlementAccountNumMasked("000-1234567-***")
                    .issueDate("20231201")
                    .build();

        } catch (Exception e) {
            log.error("카드기본정보조회 처리 중 오류 발생 - cardId: {}, userCi: {}, error: {}",
                     request.getCardId(), request.getUserCi(), e.getMessage(), e);
            return createIssueInfoErrorResponse(request, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 카드청구기본정보조회 (KFTC 요청 처리)
     */
    @Transactional(readOnly = true)
    public CardBillsResponse getCardBills(CardBillsRequest request) {
        log.info("카드청구기본정보조회 요청 처리 - fromMonth: {}, toMonth: {}, userCi: {}",
                request.getFromMonth(), request.getToMonth(), request.getUserCi());

        try {
            // 사용자 CI로 사용자 조회
            Optional<CardUser> optionalUser = cardUserRepository.findByUserCi(request.getUserCi());
            if (optionalUser.isEmpty()) {
                log.warn("사용자를 찾을 수 없음 - userCi: {}", request.getUserCi());
                return createBillsErrorResponse(request, ErrorCode.ENTITY_NOT_FOUND);
            }

            CardUser user = optionalUser.get();

            // 해지된 사용자 확인
            if (user.getStatus() == CardUser.UserStatus.WITHDRAWN) {
                log.warn("해지된 사용자 - userCi: {}", request.getUserCi());
                return createBillsErrorResponse(request, ErrorCode.ENTITY_NOT_FOUND);
            }

            String apiTranId = generateApiTranId();
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String bankTranDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 실제 청구서 목록 조회
            List<CardBillsResponse.BillInfo> billList = getBillListFromDatabase(request);

            return CardBillsResponse.builder()
                    .apiTranId(apiTranId)
                    .apiTranDtm(currentDateTime)
                    .rspCode("A0000")
                    .rspMessage("")
                    .bankTranId(request.getBankTranId())
                    .bankTranDate(bankTranDate)
                    .bankCodeTran(BANK_CODE)
                    .bankRspCode("000")
                    .bankRspMessage("")
                    .nextPageYn("N")
                    .beforInquiryTraceInfo("")
                    .billCnt(String.valueOf(billList.size()))
                    .billList(billList)
                    .build();

        } catch (Exception e) {
            log.error("카드청구기본정보조회 처리 중 오류 발생 - userCi: {}, error: {}",
                    request.getUserCi(), e.getMessage(), e);
            return createBillsErrorResponse(request, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 데이터베이스에서 실제 청구서 목록 조회
     */
    private List<CardBillsResponse.BillInfo> getBillListFromDatabase(CardBillsRequest request) {
        log.info("실제 청구서 목록 조회 시작 - userCi: {}, fromMonth: {}, toMonth: {}",
                request.getUserCi(), request.getFromMonth(), request.getToMonth());

        try {
            // 사용자 CI로 청구서 목록 조회
            List<CardBill> cardBills = cardBillRepository.findByUserCiAndChargeMonthRange(
                    request.getUserCi(),
                    request.getFromMonth(),
                    request.getToMonth()
            );

            log.info("조회된 청구서 개수: {}", cardBills.size());

            // CardBill 엔티티를 BillInfo DTO로 변환
            return cardBills.stream()
                    .map(this::convertToBillInfo)
                    .toList();

        } catch (Exception e) {
            log.error("청구서 목록 조회 실패 - userCi: {}, error: {}",
                    request.getUserCi(), e.getMessage(), e);
            return List.of(); // 빈 목록 반환
        }
    }

    /**
     * CardBill 엔티티를 BillInfo DTO로 변환
     */
    private CardBillsResponse.BillInfo convertToBillInfo(CardBill cardBill) {
        return CardBillsResponse.BillInfo.builder()
                .chargeMonth(cardBill.getChargeMonth())
                .settlementSeqNo(cardBill.getSettlementSeqNo())
                .cardId(String.valueOf(cardBill.getCard().getId()))
                .chargeAmt(cardBill.getChargeAmt().toString())
                .settlementDay(cardBill.getSettlementDay())
                .settlementDate(cardBill.getSettlementDate())
                .creditCheckType(cardBill.getCreditCheckType())
                .build();
    }

    /**
     * 카드청구상세정보조회 (KFTC 요청 처리)
     */
    @Transactional(readOnly = true)
    public CardBillDetailResponse getCardBillDetail(CardBillDetailRequest request) {
        log.info("카드청구상세정보조회 요청 처리 - chargeMonth: {}, settlementSeqNo: {}, userCi: {}",
                request.getChargeMonth(), request.getSettlementSeqNo(), request.getUserCi());

        try {
            // 사용자 CI로 사용자 조회
            Optional<CardUser> optionalUser = cardUserRepository.findByUserCi(request.getUserCi());
            if (optionalUser.isEmpty()) {
                log.warn("사용자를 찾을 수 없음 - userCi: {}", request.getUserCi());
                return createBillDetailErrorResponse(request, ErrorCode.ENTITY_NOT_FOUND);
            }

            CardUser user = optionalUser.get();

            // 해지된 사용자 확인
            if (user.getStatus() == CardUser.UserStatus.WITHDRAWN) {
                log.warn("해지된 사용자 - userCi: {}", request.getUserCi());
                return createBillDetailErrorResponse(request, ErrorCode.ENTITY_NOT_FOUND);
            }

            String apiTranId = generateApiTranId();
            String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String bankTranDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // 실제 청구서 상세 목록 조회
            List<CardBillDetailResponse.BillDetailInfo> billDetailList = getBillDetailListFromDatabase(request);

            return CardBillDetailResponse.builder()
                    .apiTranId(apiTranId)
                    .apiTranDtm(currentDateTime)
                    .rspCode("A0000")
                    .rspMessage("")
                    .bankTranId(request.getBankTranId())
                    .bankTranDate(bankTranDate)
                    .bankCodeTran(BANK_CODE)
                    .bankRspCode("000")
                    .bankRspMessage("")
                    .nextPageYn("N")
                    .beforInquiryTraceInfo("")
                    .billDetailCnt(String.valueOf(billDetailList.size()))
                    .billDetailList(billDetailList)
                    .build();

        } catch (Exception e) {
            log.error("카드청구상세정보조회 처리 중 오류 발생 - userCi: {}, error: {}",
                    request.getUserCi(), e.getMessage(), e);
            return createBillDetailErrorResponse(request, ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 데이터베이스에서 실제 청구서 상세 목록 조회
     */
    private List<CardBillDetailResponse.BillDetailInfo> getBillDetailListFromDatabase(CardBillDetailRequest request) {
        log.info("실제 청구서 상세 목록 조회 시작 - userCi: {}, chargeMonth: {}, settlementSeqNo: {}",
                request.getUserCi(), request.getChargeMonth(), request.getSettlementSeqNo());

        try {
            // 사용자 CI, 청구년월, 결제순번으로 청구서 상세 내역 조회
            List<CardBillDetail> billDetails = cardBillDetailRepository.findByUserCiAndChargeMonthAndSettlementSeqNo(
                    request.getUserCi(),
                    request.getChargeMonth(),
                    request.getSettlementSeqNo()
            );

            log.info("조회된 청구서 상세 개수: {}", billDetails.size());

            // CardBillDetail 엔티티를 BillDetailInfo DTO로 변환
            return billDetails.stream()
                    .map(this::convertToBillDetailInfo)
                    .toList();

        } catch (Exception e) {
            log.error("청구서 상세 목록 조회 실패 - userCi: {}, chargeMonth: {}, settlementSeqNo: {}, error: {}",
                    request.getUserCi(), request.getChargeMonth(), request.getSettlementSeqNo(), e.getMessage(), e);
            return List.of(); // 빈 목록 반환
        }
    }

    /**
     * CardBillDetail 엔티티를 BillDetailInfo DTO로 변환
     */
    private CardBillDetailResponse.BillDetailInfo convertToBillDetailInfo(CardBillDetail billDetail) {
        return CardBillDetailResponse.BillDetailInfo.builder()
                .cardValue(billDetail.getCardId())
                .paidDate(billDetail.getPaidDate())
                .paidTime(billDetail.getPaidTime())
                .paidAmt(billDetail.getPaidAmt().toString())
                .merchantNameMasked(billDetail.getMerchantNameMasked())
                .creditFeeAmt(billDetail.getCreditFeeAmt().toString())
                .productType(billDetail.getProductType())
                .build();
    }

    /**
     * 카드거래내역조회 (KFTC 요청 처리)
     */
    @Transactional(readOnly = true)
    public CardTransactionResponse getCardTransactions(CardTransactionRequest request) {
        log.info("카드거래내역조회 요청 처리 - userCi: {}, cardId: {}, fromDate: {}, toDate: {}",
                request.getUserCi(), request.getCardId(), request.getFromDate(), request.getToDate());

        try {
            // 1. 사용자 조회
            Optional<CardUser> optionalUser = cardUserRepository.findByUserCi(request.getUserCi());

            if (optionalUser.isEmpty()) {
                log.warn("카드거래내역조회 - 사용자를 찾을 수 없음: {}", request.getUserCi());
                return createTransactionErrorResponse(request, "555", "해당 사용자 없음");
            }

            CardUser user = optionalUser.get();

            // 2. 해지된 사용자 확인
            if (user.getStatus() == CardUser.UserStatus.WITHDRAWN) {
                log.warn("카드거래내역조회 - 해지된 사용자: {}", request.getUserCi());
                return createTransactionErrorResponse(request, "551", "기 해지 사용자");
            }

            // 3. 카드 조회
            Long cardId = Long.parseLong(request.getCardId());
            Optional<Card> optionalCard = cardRepository.findById(cardId);

            if (optionalCard.isEmpty()) {
                log.warn("카드거래내역조회 - 카드를 찾을 수 없음: {}", request.getCardId());
                return createTransactionErrorResponse(request, "556", "해당 카드 없음");
            }

            Card card = optionalCard.get();

            // 4. 카드 소유자 확인
            if (!card.getUserCi().equals(request.getUserCi())) {
                log.warn("카드거래내역조회 - 카드 소유자 불일치: cardId={}, userCi={}",
                        request.getCardId(), request.getUserCi());
                return createTransactionErrorResponse(request, "557", "카드 소유자 불일치");
            }

            // 5. 거래내역 조회
            LocalDate fromDate = LocalDate.parse(request.getFromDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate toDate = LocalDate.parse(request.getToDate(), DateTimeFormatter.ofPattern("yyyyMMdd"));

            List<CardTransaction> transactions = cardTransactionRepository.findByCardAndDateRange(
                    card, fromDate, toDate);

            // 6. 페이징 처리 (한 페이지에 20건씩)
            int pageSize = 20;
            int pageIndex = Integer.parseInt(request.getPageIndex());
            int startIndex = (pageIndex - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, transactions.size());

            List<CardTransaction> pagedTransactions = transactions.subList(startIndex, endIndex);
            boolean hasNextPage = endIndex < transactions.size();

            // 7. 응답 데이터 변환
            List<CardTransactionResponse.TransactionInfo> transactionInfoList = pagedTransactions.stream()
                    .map(this::convertToTransactionInfo)
                    .toList();

            log.info("카드거래내역조회 완료 - userCi: {}, cardId: {}, tranCnt: {}",
                    request.getUserCi(), request.getCardId(), transactionInfoList.size());

            return CardTransactionResponse.builder()
                    .apiTranId(generateApiTranId())
                    .apiTranDtm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
                    .rspCode("A0000")
                    .rspMessage("")
                    .bankTranId(request.getBankTranId())
                    .bankTranDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                    .bankCodeTran(BANK_CODE)
                    .bankRspCode("000")
                    .bankRspMessage("")
                    .nextPageYn(hasNextPage ? "Y" : "N")
                    .beforInquiryTraceInfo("")
                    .tranCnt(String.valueOf(transactionInfoList.size()))
                    .tranList(transactionInfoList)
                    .build();

        } catch (Exception e) {
            log.error("카드거래내역조회 처리 중 오류 발생 - userCi: {}, cardId: {}, error: {}",
                     request.getUserCi(), request.getCardId(), e.getMessage(), e);
            return createTransactionErrorResponse(request, "999", "시스템 오류");
        }
    }

    /**
     * CardTransaction 엔티티를 TransactionInfo DTO로 변환
     */
    private CardTransactionResponse.TransactionInfo convertToTransactionInfo(CardTransaction transaction) {
        return CardTransactionResponse.TransactionInfo.builder()
                .tranId(transaction.getTransactionId())
                .tranDate(transaction.getTranDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .tranTime(transaction.getTranTime().format(DateTimeFormatter.ofPattern("HHmmss")))
                .merchantName(transaction.getMerchantName())
                .merchantRegno(transaction.getMerchantRegno())
                .approvedAmt(transaction.getApprovedAmt().toString())
                .tranType(transaction.getTranType().getCode())
                .category(transaction.getCategory() != null ? transaction.getCategory().name() : "OTHERS")
                .memo(transaction.getMemo())
                .build();
    }

    /**
     * 거래내역조회 오류 응답 생성
     */
    private CardTransactionResponse createTransactionErrorResponse(CardTransactionRequest request, String bankRspCode, String bankRspMessage) {
        return CardTransactionResponse.builder()
                .apiTranId(generateApiTranId())
                .apiTranDtm(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")))
                .rspCode("A0001")
                .rspMessage("카드거래내역조회 실패")
                .bankTranId(request.getBankTranId())
                .bankTranDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")))
                .bankCodeTran(BANK_CODE)
                .bankRspCode(bankRspCode)
                .bankRspMessage(bankRspMessage)
                .nextPageYn("N")
                .beforInquiryTraceInfo("")
                .tranCnt("0")
                .tranList(List.of())
                .build();
    }

    // ========== Helper 메서드들 ==========

    /**
     * API 거래 ID 생성
     */
    private String generateApiTranId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }

    /**
     * 카드기본정보조회 에러 응답 생성
     */
    private CardIssueInfoResponse createIssueInfoErrorResponse(CardIssueInfoRequest request, ErrorCode errorCode) {
        String apiTranId = generateApiTranId();
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String bankTranDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return CardIssueInfoResponse.builder()
                .apiTranId(apiTranId)
                .apiTranDtm(currentDateTime)
                .rspCode("A0001")
                .rspMessage(errorCode.getMessage())
                .bankTranId(request.getBankTranId())
                .bankTranDate(bankTranDate)
                .bankCodeTran(BANK_CODE)
                .bankRspCode("999")
                .bankRspMessage("시스템 오류")
                .build();
    }

    /**
     * 카드청구기본정보조회 에러 응답 생성
     */
    private CardBillsResponse createBillsErrorResponse(CardBillsRequest request, ErrorCode errorCode) {
        String apiTranId = generateApiTranId();
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String bankTranDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return CardBillsResponse.builder()
                .apiTranId(apiTranId)
                .apiTranDtm(currentDateTime)
                .rspCode("A0001")
                .rspMessage(errorCode.getMessage())
                .bankTranId(request.getBankTranId())
                .bankTranDate(bankTranDate)
                .bankCodeTran(BANK_CODE)
                .bankRspCode("999")
                .bankRspMessage("시스템 오류")
                .nextPageYn("N")
                .beforInquiryTraceInfo("")
                .billCnt("0")
                .billList(List.of())
                .build();
    }

    /**
     * 카드청구상세정보조회 에러 응답 생성
     */
    private CardBillDetailResponse createBillDetailErrorResponse(CardBillDetailRequest request, ErrorCode errorCode) {
        String apiTranId = generateApiTranId();
        String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String bankTranDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        return CardBillDetailResponse.builder()
                .apiTranId(apiTranId)
                .apiTranDtm(currentDateTime)
                .rspCode("A0001")
                .rspMessage(errorCode.getMessage())
                .bankTranId(request.getBankTranId())
                .bankTranDate(bankTranDate)
                .bankCodeTran(BANK_CODE)
                .bankRspCode("999")
                .bankRspMessage("시스템 오류")
                .nextPageYn("N")
                .beforInquiryTraceInfo("")
                .billDetailCnt("0")
                .billDetailList(List.of())
                .build();
    }
}