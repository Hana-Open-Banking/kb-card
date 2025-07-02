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
     * 카드별 청구서 목록 조회 (최신순)
     */
    List<CardBill> findByCardOrderByBillMonthDesc(Card card);
    
    /**
     * 카드번호로 청구서 목록 조회 (최신순)
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardNo = :cardNo ORDER BY b.billMonth DESC")
    List<CardBill> findByCardNoOrderByBillMonthDesc(@Param("cardNo") String cardNo);
    
    /**
     * 카드번호와 청구월로 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardNo = :cardNo AND b.billMonth = :billMonth")
    Optional<CardBill> findByCardNoAndBillMonth(@Param("cardNo") String cardNo, @Param("billMonth") String billMonth);
    
    /**
     * 카드별 청구월로 청구서 조회
     */
    Optional<CardBill> findByCardAndBillMonth(Card card, String billMonth);
    
    /**
     * 카드별 미납 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card = :card " +
           "AND b.billStatus IN ('UNPAID', 'PARTIAL') " +
           "ORDER BY b.billMonth DESC")
    List<CardBill> findUnpaidBillsByCard(@Param("card") Card card);
    
    /**
     * 카드번호로 미납 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardNo = :cardNo " +
           "AND b.billStatus IN ('UNPAID', 'PARTIAL') " +
           "ORDER BY b.billMonth DESC")
    List<CardBill> findUnpaidBills(@Param("cardNo") String cardNo);
    
    /**
     * 카드별 연체된 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card = :card " +
           "AND (b.billStatus = 'OVERDUE' OR " +
           "(b.billStatus = 'UNPAID' AND b.payDueDate < :currentDate)) " +
           "ORDER BY b.billMonth DESC")
    List<CardBill> findOverdueBillsByCard(@Param("card") Card card, 
                                         @Param("currentDate") LocalDate currentDate);
    
    /**
     * 카드번호로 연체된 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardNo = :cardNo " +
           "AND (b.billStatus = 'OVERDUE' OR " +
           "(b.billStatus = 'UNPAID' AND b.payDueDate < :currentDate)) " +
           "ORDER BY b.billMonth DESC")
    List<CardBill> findOverdueBills(@Param("cardNo") String cardNo, 
                                   @Param("currentDate") LocalDate currentDate);
    
    /**
     * 카드별 특정 기간의 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card = :card " +
           "AND b.billMonth >= :startMonth AND b.billMonth <= :endMonth " +
           "ORDER BY b.billMonth DESC")
    List<CardBill> findBillsByCardAndPeriod(@Param("card") Card card,
                                           @Param("startMonth") String startMonth,
                                           @Param("endMonth") String endMonth);
    
    /**
     * 카드번호로 특정 기간의 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardNo = :cardNo " +
           "AND b.billMonth >= :startMonth AND b.billMonth <= :endMonth " +
           "ORDER BY b.billMonth DESC")
    List<CardBill> findBillsByPeriod(@Param("cardNo") String cardNo,
                                    @Param("startMonth") String startMonth,
                                    @Param("endMonth") String endMonth);
    
    /**
     * 청구서 ID로 조회 (중복 체크용)
     */
    boolean existsByBillId(String billId);
    
    /**
     * 카드별 최신 청구서 조회
     */
    Optional<CardBill> findFirstByCardOrderByBillMonthDesc(Card card);
    
    /**
     * 카드번호로 최신 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardNo = :cardNo ORDER BY b.billMonth DESC LIMIT 1")
    Optional<CardBill> findFirstByCardNoOrderByBillMonthDesc(@Param("cardNo") String cardNo);
    
    /**
     * 카드별 총 미납액 조회
     */
    @Query("SELECT SUM(b.billAmt - b.paidAmt) FROM CardBill b " +
           "WHERE b.card = :card AND b.billStatus IN ('UNPAID', 'PARTIAL')")
    Optional<java.math.BigDecimal> getTotalUnpaidAmountByCard(@Param("card") Card card);
    
    /**
     * 카드번호로 총 미납액 조회
     */
    @Query("SELECT SUM(b.billAmt - b.paidAmt) FROM CardBill b " +
           "WHERE b.card.cardNo = :cardNo AND b.billStatus IN ('UNPAID', 'PARTIAL')")
    Optional<java.math.BigDecimal> getTotalUnpaidAmount(@Param("cardNo") String cardNo);
    
    /**
     * 사용자별 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardUser.userId = :userId ORDER BY b.billMonth DESC")
    List<CardBill> findByUserId(@Param("userId") String userId);
    
    /**
     * 사용자 CI별 청구서 조회
     */
    @Query("SELECT b FROM CardBill b WHERE b.card.cardUser.userCi = :userCi ORDER BY b.billMonth DESC")
    List<CardBill> findByUserCi(@Param("userCi") String userCi);
} 