package com.kb_card.card.repository;

import com.kb_card.card.entity.CardApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardApplicationRepository extends JpaRepository<CardApplication, Long> {
    
    /**
     * 신청 ID로 조회
     */
    Optional<CardApplication> findByApplicationId(String applicationId);
    
    /**
     * 이메일로 신청 이력 조회
     */
    List<CardApplication> findByEmailOrderByApplicationDateDesc(String email);
    
    /**
     * 전화번호로 신청 이력 조회
     */
    List<CardApplication> findByPhoneOrderByApplicationDateDesc(String phone);
    
    /**
     * 상태별 신청 조회
     */
    List<CardApplication> findByApplicationStatusOrderByApplicationDateDesc(
            CardApplication.ApplicationStatus status);
    
    /**
     * 심사중인 신청 조회 (오래된 순)
     */
    @Query("SELECT a FROM CardApplication a WHERE a.applicationStatus = 'PENDING' " +
           "ORDER BY a.applicationDate ASC")
    List<CardApplication> findPendingApplications();
    
    /**
     * 특정 기간의 신청 조회
     */
    @Query("SELECT a FROM CardApplication a WHERE a.applicationDate >= :startDate " +
           "AND a.applicationDate <= :endDate ORDER BY a.applicationDate DESC")
    List<CardApplication> findApplicationsByPeriod(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
    
    /**
     * 발급된 카드번호로 신청 조회
     */
    Optional<CardApplication> findByIssuedCardNo(String cardNo);
    
    /**
     * 신청 ID 존재 여부 확인
     */
    boolean existsByApplicationId(String applicationId);
    
    /**
     * 이메일과 전화번호로 최근 신청 조회 (중복 신청 체크용)
     */
    @Query("SELECT a FROM CardApplication a WHERE a.email = :email AND a.phone = :phone " +
           "AND a.applicationDate >= :recentDate ORDER BY a.applicationDate DESC LIMIT 1")
    Optional<CardApplication> findRecentApplicationByEmailAndPhone(
            @Param("email") String email, 
            @Param("phone") String phone,
            @Param("recentDate") LocalDateTime recentDate);
    
    /**
     * 월별 신청 통계
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', a.applicationDate, '%Y-%m') as month, " +
           "COUNT(a) as count, a.applicationStatus " +
           "FROM CardApplication a " +
           "WHERE a.applicationDate >= :startDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', a.applicationDate, '%Y-%m'), a.applicationStatus " +
           "ORDER BY month DESC")
    List<Object[]> getMonthlyApplicationStatistics(@Param("startDate") LocalDateTime startDate);
} 