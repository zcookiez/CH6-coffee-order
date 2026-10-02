package com.sparta.coffee.domain.order.service;

import com.sparta.coffee.domain.menu.entity.Menu;
import com.sparta.coffee.domain.menu.repository.MenuRepository;
import com.sparta.coffee.domain.order.dto.OrderRequest;
import com.sparta.coffee.domain.order.repository.OrderRepository;
import com.sparta.coffee.domain.point.entity.Point;
import com.sparta.coffee.domain.point.repository.PointHistoryRepository;
import com.sparta.coffee.domain.point.repository.PointRepository;
import com.sparta.coffee.domain.user.entity.User;
import com.sparta.coffee.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderServiceConcurrencyTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    private User testUser;
    private Menu testMenu;

    @BeforeEach
    void setUp() {
        // 1. 테스트 유저 생성
        testUser = User.builder().name("tester").build();
        userRepository.save(testUser);

        // 2. 테스트 메뉴 생성 (재고 100개, 가격 1000원)
        testMenu = Menu.builder()
                .name("아이스 아메리카노")
                .price(1000)
                .stock(100)
                .build();
        menuRepository.save(testMenu);

        // 3. 테스트 유저에게 포인트 20000원 충전
        Point point = Point.builder()
                .userId(testUser.getId())
                .balance(20000L)
                .build();
        pointRepository.save(point);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll(); // 연관된 order_items까지 cascade 삭제되도록 변경
        pointHistoryRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        menuRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("다수 유저의 단일 메뉴 동시 주문: DB 원자적 쿼리를 통해 재고 초과 판매(Overselling)를 방어한다")
    void orderConcurrency_StockTest() throws InterruptedException {
        // given
        // 테스트 메뉴 재고는 10개. 15명의 유저가 동시에 1개씩 주문 시도
        int threadCount = 15;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 15명의 임시 유저 및 포인트(충분함) 생성
        List<User> users = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            User u = userRepository.save(User.builder().name("user" + i).build());
            pointRepository.save(Point.builder().userId(u.getId()).balance(5000L).build());
            users.add(u);
        }

        // 재고 고갈 테스트를 위해 이 테스트 전용 메뉴(재고 10개)를 별도 생성합니다.
        Menu limitedMenu = menuRepository.save(
                com.sparta.coffee.domain.menu.entity.Menu.builder()
                        .name("선착순 한정판 커피")
                        .price(1000)
                        .stock(10)
                        .build()
        );

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executorService.submit(() -> {
                try {
                    OrderRequest request = new OrderRequest(users.get(index).getId(), limitedMenu.getId(), 1);
                    orderService.createOrder(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Menu updatedMenu = menuRepository.findById(limitedMenu.getId()).orElseThrow();

        // 재고는 10개뿐이므로, 15명이 시도해도 딱 10명만 성공하고 5명은 실패(재고 부족)해야 함
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(5);
        
        // 최종 재고는 0이어야 함 (마이너스가 되면 안 됨!)
        assertThat(updatedMenu.getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("단일 유저의 따닥(중복) 주문: Redis 분산 락을 통해 큐잉되어 잔액 부족 시 정확히 차단된다")
    void orderConcurrency_UserPointTest() throws InterruptedException {
        // given
        // 테스트 유저 포인트는 20,000원. 메뉴 가격은 1,000원.
        // 수량을 5개(5,000원)씩 동시에 5번 주문 시도 (총 25,000원 필요)
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        OrderRequest request = new OrderRequest(testUser.getId(), testMenu.getId(), 5);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    orderService.createOrder(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Point updatedPoint = pointRepository.findByUserId(testUser.getId()).orElseThrow();

        // 20000원으로 5000원짜리 주문을 5번 시도했으므로, 4번 성공(20000원)하고 1번은 잔액 부족으로 실패해야 함
        assertThat(successCount.get()).isEqualTo(4);
        assertThat(failCount.get()).isEqualTo(1);

        // 최종 잔액은 0원이어야 함 (순차적으로 차감되었음이 증명됨)
        assertThat(updatedPoint.getBalance()).isEqualTo(0L);
    }
}
