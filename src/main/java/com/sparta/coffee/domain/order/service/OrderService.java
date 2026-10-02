package com.sparta.coffee.domain.order.service;

import com.sparta.coffee.domain.menu.entity.Menu;
import com.sparta.coffee.domain.menu.repository.MenuRepository;
import com.sparta.coffee.domain.order.dto.OrderRequest;
import com.sparta.coffee.domain.order.dto.OrderResponse;
import com.sparta.coffee.domain.order.entity.Order;
import com.sparta.coffee.domain.order.entity.OrderItem;
import com.sparta.coffee.domain.order.entity.OrderStatus;
import com.sparta.coffee.domain.order.event.OrderCompletedEvent;
import com.sparta.coffee.domain.order.repository.OrderRepository;
import com.sparta.coffee.domain.point.service.PointService;
import com.sparta.coffee.domain.user.service.UserService;
import com.sparta.coffee.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuRepository menuRepository;
    private final UserService userService;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 커피 주문 및 결제 핵심 로직
     */
    @DistributedLock(key = "'lock:user:' + #request.userId()")
    public OrderResponse createOrder(OrderRequest request) {
        // 1. 유저 검증
        userService.validateUserExists(request.userId());

        // 2. 메뉴 검증 및 가격 조회
        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 메뉴입니다."));

        long totalPrice = (long) menu.getPrice() * request.quantity();

        // 3. 재고 차감 (원자적 쿼리)
        int updatedRows = menuRepository.reduceStock(menu.getId(), request.quantity());
        if (updatedRows == 0) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        // 4. 포인트 차감
        // UUID는 36자이므로 DB 컬럼(length=36)에 딱 맞습니다. ("PAY-" 접두사를 빼서 길이 초과 방지)
        String transactionId = UUID.randomUUID().toString();
        pointService.usePoint(request.userId(), totalPrice, transactionId);

        // 5. 주문 생성 및 저장
        Order order = Order.builder()
                .userId(request.userId())
                .totalPrice(totalPrice)
                .status(OrderStatus.COMPLETED)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .menuId(menu.getId())
                .quantity(request.quantity())
                .orderPrice((long) menu.getPrice())
                .build();

        order.addOrderItem(orderItem);
        orderRepository.save(order);

        // 6. Kafka 발행을 위한 사내 애플리케이션 이벤트 발행 (트랜잭션 커밋 후 동작하도록 설계)
        eventPublisher.publishEvent(new OrderCompletedEvent(request.userId(), request.menuId(), order.getTotalPrice()));

        return OrderResponse.from(order);
    }
}
