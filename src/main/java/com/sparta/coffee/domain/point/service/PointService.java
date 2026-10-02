package com.sparta.coffee.domain.point.service;

import com.sparta.coffee.domain.point.dto.PointChargeRequest;
import com.sparta.coffee.domain.point.dto.PointResponse;
import com.sparta.coffee.domain.point.entity.Point;
import com.sparta.coffee.domain.point.entity.PointHistory;
import com.sparta.coffee.domain.point.entity.PointType;
import com.sparta.coffee.domain.point.repository.PointHistoryRepository;
import com.sparta.coffee.domain.point.repository.PointRepository;
import com.sparta.coffee.domain.user.service.UserService;
import com.sparta.coffee.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserService userService;

    /**
     * 포인트 충전 (Redisson 분산 락 + 멱등성 보장)
     */
    @DistributedLock(key = "'lock:user:' + #request.userId()")
    //@DistributedLock(key = "'lock:user:' + #request.userId()", waitTime = 100)
    public PointResponse chargePoint(PointChargeRequest request) {
        // 1. 유저 존재 여부 검증
        userService.validateUserExists(request.userId());

        // 2. 멱등성 검증 (이미 처리된 요청인지 확인)
        if (pointHistoryRepository.existsByChargeRequestId(request.chargeRequestId())) {
            throw new IllegalArgumentException("이미 처리된 충전 요청입니다.");
        }

        // 3. 포인트 잔액 조회 및 업데이트 (없으면 신규 생성)
        Point point = pointRepository.findByUserId(request.userId())
                .orElse(Point.builder()
                        .userId(request.userId())
                        .balance(0L)
                        .build());
        
        point.addBalance(request.amount());
        pointRepository.save(point);

        // 4. 충전 내역 저장
        PointHistory history = PointHistory.builder()
                .userId(request.userId())
                .type(PointType.CHARGE)
                .amount(request.amount())
                .balanceAfter(point.getBalance())
                .chargeRequestId(request.chargeRequestId())
                .build();
        pointHistoryRepository.save(history);

        // 5. 결과 반환
        return PointResponse.from(point);
    }

    /**
     * 포인트 차감 (주문 시 호출됨, 외부 트랜잭션 및 락에 참여)
     */
    public void usePoint(Long userId, Long amount, String transactionId) {
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("포인트 정보가 없습니다."));

        if (point.getBalance() < amount) {
            throw new IllegalArgumentException("포인트 잔액이 부족합니다.");
        }

        point.subtractBalance(amount);
        pointRepository.save(point);

        PointHistory history = PointHistory.builder()
                .userId(userId)
                .type(PointType.USE)
                .amount(amount)
                .balanceAfter(point.getBalance())
                .chargeRequestId(transactionId) // 멱등성 키 재사용 (주문 결제 식별자)
                .build();
        pointHistoryRepository.save(history);
    }
}
