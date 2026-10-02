package com.sparta.coffee.domain.order.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-events";

    /**
     * 주문 트랜잭션이 성공적으로 COMMIT 된 직후에만 실행됩니다.
     * DB 롤백 시 이 이벤트 리스너는 호출되지 않아, 유령 이벤트 발행을 방지합니다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCompletedEvent(OrderCompletedEvent event) {
        log.info("주문 트랜잭션 커밋 완료. Kafka 로 이벤트를 발행합니다: {}", event);
        
        try {
            // Kafka 이벤트 발행 (유저 ID를 Key로 사용하여 파티션 순서 보장)
            kafkaTemplate.send(TOPIC, String.valueOf(event.userId()), event);
            log.info("Kafka 이벤트 발행 성공 - Topic: {}, UserId: {}", TOPIC, event.userId());
        } catch (Exception e) {
            // Kafka 발행 실패 시 로그만 남기고 주문 트랜잭션을 롤백시키지 않음 (의도적 격리)
            log.error("Kafka 이벤트 발행 중 오류 발생 (하지만 주문은 이미 성공했습니다) - UserId: {}", event.userId(), e);
        }
    }
}
