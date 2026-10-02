package com.sparta.coffee.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Long menuId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Long orderPrice; // 주문 시점의 메뉴 단가 (스냅샷)

    @Builder
    public OrderItem(Long menuId, int quantity, Long orderPrice) {
        this.menuId = menuId;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    // 연관관계 편의 메서드 (Order 측에서 호출됨)
    void assignOrder(Order order) {
        this.order = order;
    }
}
