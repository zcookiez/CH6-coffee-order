package com.sparta.coffee.domain.menu.event;

import com.sparta.coffee.domain.menu.repository.MenuDailyStatsRepository;
import com.sparta.coffee.domain.order.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuStatsEventListener {

    private final MenuDailyStatsRepository menuDailyStatsRepository;

    /**
     * 주문 결제 완료(COMMIT) 후 비동기로 동작하는 통계 적재 리스너.
     * 비동기(@Async)로 동작하여 통계 DB에서 에러가 발생해도 고객의 주문 결제를 롤백시키지 않습니다.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompletedStats(OrderCompletedEvent event) {
        log.info("주문 완료 통계 업데이트 이벤트 수신: menuId={}, quantity={}", event.menuId(), event.quantity());
        try {
            menuDailyStatsRepository.upsertOrderCount(event.menuId(), LocalDate.now(), event.quantity());
        } catch (Exception e) {
            log.error("통계 업데이트 실패 (주문은 성공적으로 완료됨) - menuId: {}", event.menuId(), e);
        }
    }
}