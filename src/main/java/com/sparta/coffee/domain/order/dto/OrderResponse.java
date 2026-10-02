package com.sparta.coffee.domain.order.dto;

import com.sparta.coffee.domain.order.entity.Order;
import com.sparta.coffee.domain.order.entity.OrderStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record OrderResponse(
        Long orderId,
        Long userId,
        Long totalPrice,
        OrderStatus status,
        LocalDateTime createdAt
) {
    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
