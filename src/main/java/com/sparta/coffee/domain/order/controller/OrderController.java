package com.sparta.coffee.domain.order.controller;

import com.sparta.coffee.domain.order.dto.OrderRequest;
import com.sparta.coffee.domain.order.dto.OrderResponse;
import com.sparta.coffee.domain.order.service.OrderService;
import com.sparta.coffee.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "주문 API", description = "커피 주문 및 결제 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "커피 주문 및 결제", description = "메뉴를 주문하고 포인트를 차감하여 결제합니다. (동시성 제어 적용)")
    @PostMapping
    public CommonResponse<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return CommonResponse.success(response);
    }
}
