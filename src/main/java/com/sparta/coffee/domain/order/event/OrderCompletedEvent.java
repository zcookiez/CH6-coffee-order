package com.sparta.coffee.domain.order.event;

public record OrderCompletedEvent(
        Long userId,
        Long menuId,
        int quantity,
        Long totalPrice
) {
}
