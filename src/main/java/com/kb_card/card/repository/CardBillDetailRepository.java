package com.kb_card.card.repository;

import com.kb_card.card.entity.CardBill;
import com.kb_card.card.entity.CardBillDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CardBillDetailRepository extends JpaRepository<CardBillDetail, Long> {
    
    /**
     * 청구서별 상세 내역 조회 (사용일시 기준 내림차순)
     */
    List<CardBillDetail> findByCardBillOrderByPaidDateDescPaidTimeDesc(CardBill cardBill);
    
    /**
     * 청구서 ID로 상세 내역 조회 (사용일시 기준 내림차순)
     */
    @Query("SELECT d FROM CardBillDetail d WHERE d.cardBill.id = :cardBillId ORDER BY d.paidDate DESC, d.paidTime DESC")
    List<CardBillDetail> findByCardBillIdOrderByPaidDateDescPaidTimeDesc(@Param("cardBillId") Long cardBillId);
    
    /**
     * 청구서별 상세 내역 개수 조회
     */
    long countByCardBill(CardBill cardBill);
    
    /**
     * 청구서 ID로 상세 내역 개수 조회
     */
    @Query("SELECT COUNT(d) FROM CardBillDetail d WHERE d.cardBill.id = :cardBillId")
    long countByCardBillId(@Param("cardBillId") Long cardBillId);
    
    /**
     * 사용자 CI와 청구년월, 결제순번으로 상세 내역 조회
     */
    @Query("SELECT d FROM CardBillDetail d " +
           "WHERE d.cardBill.card.cardUser.userCi = :userCi " +
           "AND d.cardBill.chargeMonth = :chargeMonth " +
           "AND d.cardBill.settlementSeqNo = :settlementSeqNo " +
           "ORDER BY d.paidDate DESC, d.paidTime DESC")
    List<CardBillDetail> findByUserCiAndChargeMonthAndSettlementSeqNo(
            @Param("userCi") String userCi,
            @Param("chargeMonth") String chargeMonth,
            @Param("settlementSeqNo") String settlementSeqNo
    );
    
    /**
     * 카드별 특정 월의 상세 내역 조회
     */
    @Query("SELECT d FROM CardBillDetail d " +
           "WHERE d.cardBill.card.cardNo = :cardNo " +
           "AND d.cardBill.chargeMonth = :chargeMonth " +
           "ORDER BY d.paidDate DESC, d.paidTime DESC")
    List<CardBillDetail> findByCardNoAndChargeMonth(
            @Param("cardNo") String cardNo,
            @Param("chargeMonth") String chargeMonth
    );
    
    /**
     * 청구서별 총 이용금액 조회
     */
    @Query("SELECT SUM(d.paidAmt) FROM CardBillDetail d WHERE d.cardBill = :cardBill")
    BigDecimal getTotalAmountByCardBill(@Param("cardBill") CardBill cardBill);
    
    /**
     * 청구서별 총 수수료 조회
     */
    @Query("SELECT SUM(d.creditFeeAmt) FROM CardBillDetail d WHERE d.cardBill = :cardBill")
    BigDecimal getTotalCreditFeeByCardBill(@Param("cardBill") CardBill cardBill);
}