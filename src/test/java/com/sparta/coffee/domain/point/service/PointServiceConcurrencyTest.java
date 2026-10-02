package com.sparta.coffee.domain.point.service;

import com.sparta.coffee.domain.point.dto.PointChargeRequest;
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

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 동시성 테스트를 위해 유저를 미리 하나 생성합니다.
        testUser = User.builder()
                .name("tester")
                .build();
        userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // 반복적인 테스트 실행을 위해 데이터를 비워줍니다.
        pointHistoryRepository.deleteAllInBatch();
        pointRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("동시성 제어(분산락) 테스트: 완전히 다른 결제 건들이 동시에 들어오면 누락(Lost Update) 없이 모두 합산되어야 한다")
    void chargePointConcurrency_LockTest() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    // 매번 다른 UUID를 가진 "정상적인 서로 다른 충전 요청" 10개 생성
                    PointChargeRequest request = new PointChargeRequest(
                            testUser.getId(), 
                            1000L, 
                            UUID.randomUUID().toString()
                    );
                    pointService.chargePoint(request);
                    successCount.incrementAndGet(); // 성공 횟수 증가
                } catch (Exception e) {
                    failCount.incrementAndGet(); // 에러 횟수 증가
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Point point = pointRepository.findByUserId(testUser.getId()).orElseThrow();
        
        // 1. 요청 10개가 모두 다른 결제 건이므로 10번 모두 성공해야 함
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(0);
        
        // 2. 동시성 제어(락)가 완벽하게 작동했다면, 잔액은 1000원 * 10번 = 10000원이 정확히 되어야 함
        assertThat(point.getBalance()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("멱등성 제어 테스트: 완전히 동일한 결제 ID로 동시에 요청이 오면 1번만 결제되고 나머지는 차단되어야 한다")
    void chargePointConcurrency_IdempotencyTest() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // "버튼을 동시에 다다닥 누른 상황" 가정: 완전히 동일한 하나의 요청 객체
        PointChargeRequest request = new PointChargeRequest(
                testUser.getId(), 
                5000L, 
                "duplicate-req-1234"
        );

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    pointService.chargePoint(request);
                    successCount.incrementAndGet(); // 성공 횟수 증가
                } catch (Exception e) {
                    failCount.incrementAndGet(); // 에러(중복) 횟수 증가
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // then
        Point point = pointRepository.findByUserId(testUser.getId()).orElseThrow();
        
        // 1. 멱등성 방어에 의해 1번만 성공하고 9번은 실패해야 함
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);
        
        // 2. 결과적으로 유저의 잔액은 딱 5000원만 늘어나 있어야 함
        assertThat(point.getBalance()).isEqualTo(5000L);
    }
}
