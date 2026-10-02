package com.sparta.coffee.domain.point.entity;

import com.sparta.coffee.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "points")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private Long balance; // 최종 잔액

    @Builder
    public Point(Long userId, Long balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public void addBalance(Long amount) {
        this.balance += amount;
    }

    public void subtractBalance(Long amount) {
        this.balance -= amount;
    }
}
