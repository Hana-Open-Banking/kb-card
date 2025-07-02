package com.kb_card.card.entity;

import com.kb_card.common.domain.DateTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "card_users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardUser extends DateTimeEntity {
    /**
     * 카드사 내부 사용자 식별자 (UUID)
     */
    @Id
    @Column(name = "user_id", length = 36) // UUID는 36자
    @Builder.Default
    private String userId = UUID.randomUUID().toString();

    /**
     * 외부 연계용 사용자 CI (OpenBanking 표준)
     */
    @Column(name = "user_ci", unique = true, nullable = false, length = 120)
    private String userCi;
    
    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;
    
    @Column(name = "user_email", nullable = false, length = 100)
    private String userEmail;

    @Column(name = "user_phone", nullable = false, length = 15)
    private String userPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    public enum UserStatus {
        ACTIVE,      // 정상
        INACTIVE,    // 비활성
        WITHDRAWN    // 탈퇴
    }
    
    public void withdraw() {
        this.status = UserStatus.WITHDRAWN;
    }
    
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    public void updateEmail(String newEmail) {
        this.userEmail = newEmail;
    }
} 