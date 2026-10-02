package com.sparta.coffee.domain.order.repository;

import com.sparta.coffee.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
