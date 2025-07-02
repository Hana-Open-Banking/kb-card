package com.kb_card.card.repository;

import com.kb_card.card.entity.Card;
import com.kb_card.card.entity.CardTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardTransactionRepository extends JpaRepository<CardTransaction, Long> {
    
    /**
     * 카드별 거래 내역 조회 (페이징)
     */
    Page<CardTransaction> findByCardOrderByTranDateDescTranTimeDesc(Card card, Pageable pageable);
    
    /**
     * 카드번호로 거래 내역 조회 (페이징)
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card.cardNo = :cardNo ORDER BY t.tranDate DESC, t.tranTime DESC")
    Page<CardTransaction> findByCardNoOrderByTranDateDescTranTimeDesc(@Param("cardNo") String cardNo, Pageable pageable);
    
    /**
     * 카드별 기간으로 거래 내역 조회
     */
    List<CardTransaction> findByCardAndTranDateBetweenOrderByTranDateDescTranTimeDesc(
            Card card, LocalDate startDate, LocalDate endDate);
    
    /**
     * 카드별 카테고리로 거래 내역 조회
     */
    List<CardTransaction> findByCardAndCategoryOrderByTranDateDescTranTimeDesc(
            Card card, CardTransaction.TransactionCategory category);
    
    /**
     * 카드번호와 기간으로 거래 내역 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card.cardNo = :cardNo " +
           "AND t.tranDate >= :startDate AND t.tranDate <= :endDate " +
           "ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByCardNoAndTranDateBetweenOrderByTranDateDescTranTimeDesc(
            @Param("cardNo") String cardNo, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    /**
     * 카드번호와 카테고리로 거래 내역 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card.cardNo = :cardNo AND t.category = :category " +
           "ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByCardNoAndCategoryOrderByTranDateDescTranTimeDesc(
            @Param("cardNo") String cardNo, @Param("category") CardTransaction.TransactionCategory category);
    
    /**
     * 카드별 자동차 관련 거래 내역만 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card = :card " +
           "AND t.category IN ('FUEL', 'TOLL', 'PARKING', 'MAINTENANCE') " +
           "ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findCarRelatedTransactionsByCard(@Param("card") Card card);
    
    /**
     * 카드번호로 자동차 관련 거래 내역만 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card.cardNo = :cardNo " +
           "AND t.category IN ('FUEL', 'TOLL', 'PARKING', 'MAINTENANCE') " +
           "ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findCarRelatedTransactions(@Param("cardNo") String cardNo);
    
    /**
     * 카드별 월별 거래 통계
     */
    @Query("SELECT t.category, SUM(t.approvedAmt) " +
           "FROM CardTransaction t " +
           "WHERE t.card = :card " +
           "AND t.tranDate >= :startDate AND t.tranDate <= :endDate " +
           "GROUP BY t.category")
    List<Object[]> getCategoryStatisticsByCard(@Param("card") Card card, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    /**
     * 카드번호로 월별 거래 통계
     */
    @Query("SELECT t.category, SUM(t.approvedAmt) " +
           "FROM CardTransaction t " +
           "WHERE t.card.cardNo = :cardNo " +
           "AND t.tranDate >= :startDate AND t.tranDate <= :endDate " +
           "GROUP BY t.category")
    List<Object[]> getCategoryStatistics(@Param("cardNo") String cardNo, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    /**
     * 거래 ID로 조회 (중복 체크용)
     */
    boolean existsByTransactionId(String transactionId);
    
    /**
     * 카드별 최근 거래 내역 조회 (개수 제한)
     */
    List<CardTransaction> findTop10ByCardOrderByTranDateDescTranTimeDesc(Card card);
    
    /**
     * 카드번호로 최근 거래 내역 조회 (개수 제한)
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card.cardNo = :cardNo " +
           "ORDER BY t.tranDate DESC, t.tranTime DESC LIMIT 10")
    List<CardTransaction> findTop10ByCardNoOrderByTranDateDescTranTimeDesc(@Param("cardNo") String cardNo);
    
    /**
     * 사용자별 거래 내역 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card.cardUser.userId = :userId " +
           "ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByUserId(@Param("userId") String userId);
    
    /**
     * 사용자 CI별 거래 내역 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card.cardUser.userCi = :userCi " +
           "ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByUserCi(@Param("userCi") String userCi);
    
    /**
     * 카드별 거래내역 조회
     */
    List<CardTransaction> findByCard(Card card);
    
    /**
     * 카드별 날짜 범위 거래내역 조회 (날짜 오름차순)
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card = :card AND t.tranDate BETWEEN :fromDate AND :toDate ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByCardAndDateRange(@Param("card") Card card, 
                                                @Param("fromDate") LocalDate fromDate, 
                                                @Param("toDate") LocalDate toDate);
    
    /**
     * 거래 ID로 거래내역 조회
     */
    Optional<CardTransaction> findByTransactionId(String transactionId);
    
    /**
     * 카드별 거래구분별 거래내역 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card = :card AND t.tranType = :tranType ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByCardAndTranType(@Param("card") Card card, 
                                              @Param("tranType") CardTransaction.TransactionType tranType);
    
    /**
     * 특정 기간 동안의 카드별 승인 거래내역 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card = :card AND t.tranDate BETWEEN :fromDate AND :toDate AND t.tranType = 'APPROVAL' ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findApprovalTransactionsByCardAndDateRange(@Param("card") Card card, 
                                                                    @Param("fromDate") LocalDate fromDate, 
                                                                    @Param("toDate") LocalDate toDate);
    
    /**
     * 가맹점명으로 거래내역 검색
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card = :card AND t.merchantName LIKE %:merchantName% ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByCardAndMerchantNameContaining(@Param("card") Card card, 
                                                            @Param("merchantName") String merchantName);
    
    /**
     * 특정 카테고리의 거래내역 조회
     */
    @Query("SELECT t FROM CardTransaction t WHERE t.card = :card AND t.category = :category ORDER BY t.tranDate DESC, t.tranTime DESC")
    List<CardTransaction> findByCardAndCategory(@Param("card") Card card, 
                                              @Param("category") CardTransaction.TransactionCategory category);
    
    /**
     * 카드별 거래내역 통계 (총 거래건수)
     */
    @Query("SELECT COUNT(t) FROM CardTransaction t WHERE t.card = :card AND t.tranDate BETWEEN :fromDate AND :toDate")
    long countByCardAndDateRange(@Param("card") Card card, 
                                @Param("fromDate") LocalDate fromDate, 
                                @Param("toDate") LocalDate toDate);
    
    /**
     * 카드별 거래내역 통계 (총 거래금액)
     */
    @Query("SELECT SUM(t.approvedAmt) FROM CardTransaction t WHERE t.card = :card AND t.tranDate BETWEEN :fromDate AND :toDate AND t.tranType = 'APPROVAL'")
    java.math.BigDecimal sumApprovedAmountByCardAndDateRange(@Param("card") Card card, 
                                                           @Param("fromDate") LocalDate fromDate, 
                                                           @Param("toDate") LocalDate toDate);
} 