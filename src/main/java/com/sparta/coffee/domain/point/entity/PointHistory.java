package com.sparta.coffee.domain.point.entity;

import com.sparta.coffee.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "point_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PointType type;

    @Column(nullable = false)
    private Long amount; // 변동된 포인트 금액

    @Column(nullable = false)
    private Long balanceAfter; // 거래 직후 잔여 포인트

    // 멱등성 보장을 위한 클라이언트 충전 요청 고유 식별자
    @Column(name = "charge_request_id", length = 36, unique = true, nullable = false)
    private String chargeRequestId;

    @Builder
    public PointHistory(Long userId, PointType type, Long amount, Long balanceAfter, String chargeRequestId) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.chargeRequestId = chargeRequestId;
    }
}
