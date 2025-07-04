package com.kb_card.card.repository;

import com.kb_card.card.entity.Card;
import com.kb_card.card.entity.CardBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardBillRepository extends JpaRepository<CardBill, Long> {
    
    
       /**
        * 사용자 CI와 청구년월 범위로 청구서 조회 (결제년월일 기준 내림차순)
        */
       @Query("SELECT b FROM CardBill b WHERE b.card.cardUser.userCi = :userCi " +
              "AND b.chargeMonth >= :fromMonth AND b.chargeMonth <= :toMonth " +
              "ORDER BY b.settlementDate DESC, b.chargeMonth DESC")
       List<CardBill> findByUserCiAndChargeMonthRange(
              @Param("userCi") String userCi,
              @Param("fromMonth") String fromMonth,
              @Param("toMonth") String toMonth
       );

       /**
        * 사용자 CI와 청구년월, 결제순번으로 청구서 조회
        */
       @Query("SELECT b FROM CardBill b WHERE b.card.cardUser.userCi = :userCi " +
              "AND b.chargeMonth = :chargeMonth AND b.settlementSeqNo = :settlementSeqNo")
       Optional<CardBill> findByUserCiAndChargeMonthAndSettlementSeqNo(
              @Param("userCi") String userCi,
              @Param("chargeMonth") String chargeMonth,
              @Param("settlementSeqNo") String settlementSeqNo
       );

       /**
        * 카드별 현재 활성 청구서 조회 (이번 달 청구서)
        */
       @Query("SELECT b FROM CardBill b WHERE b.card = :card AND b.billStatus = 'ACTIVE'")
       Optional<CardBill> findActiveCardBill(@Param("card") Card card);

       /**
        * 사용자 CI로 현재 활성 청구서 조회
        */
       @Query("SELECT b FROM CardBill b WHERE b.card.cardUser.userCi = :userCi AND b.billStatus = 'ACTIVE'")
       List<CardBill> findActiveCardBillsByUserCi(@Param("userCi") String userCi);

       /**
        * 카드별 청구월로 청구서 조회
        */
       Optional<CardBill> findByCardAndChargeMonth(Card card, String chargeMonth);

       /**
        * 청구월별 모든 청구서 조회
        */
       List<CardBill> findByChargeMonth(String chargeMonth);

       /**
        * 청구월과 상태별 청구서 조회
        */
       List<CardBill> findByChargeMonthAndBillStatus(String chargeMonth, CardBill.BillStatus billStatus);
}      