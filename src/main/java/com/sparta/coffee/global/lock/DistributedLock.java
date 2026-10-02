package com.sparta.coffee.global.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산 락을 적용할 메서드에 부착하는 커스텀 어노테이션입니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /**
     * 락의 이름
     * 예: "'lock:user:' + #userId"
     */
    String key();

    /**
     * 락 획득을 위해 대기할 시간
     */
    long waitTime() default 5L;

    /**
     * 락 획득 후 유지(점유)할 시간
     * -1로 설정 시 Redisson의 Watchdog이 작동하여 작업이 끝날 때까지 락을 무한 연장합니다.
     */
    long leaseTime() default -1L;

    /**
     * 시간 단위 (기본값: 초)
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
