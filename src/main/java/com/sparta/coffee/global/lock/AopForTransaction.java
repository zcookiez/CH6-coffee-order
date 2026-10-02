package com.sparta.coffee.global.lock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 락 해제 전 트랜잭션 커밋을 강제하기 위한 AOP 래퍼 클래스
 */
@Component
public class AopForTransaction {

    /**
     * Propagation.REQUIRES_NEW 를 사용하여 부모 트랜잭션 유무와 무관하게 
     * 항상 독립적인 새 트랜잭션을 시작하고 커밋함.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object proceed(final ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
