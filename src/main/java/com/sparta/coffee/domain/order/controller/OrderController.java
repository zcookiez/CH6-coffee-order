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

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public CommonResponse<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return CommonResponse.success(response);
    }
}
