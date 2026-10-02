package com.sparta.coffee.domain.order.entity;

public enum OrderStatus {
    COMPLETED, // 결제 완료 (정상 처리)
    CANCELED,  // 주문 취소
    FAILED     // 결제 실패 (잔액 부족 등)
}
