package com.sparta.coffee.global.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.sparta.coffee.global.lock.DistributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = parseKey(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        RLock rLock = redissonClient.getLock(key);

        try {
            log.debug("Try to acquire lock [{}]", key);
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                log.warn("Lock acquisition failed [{}]", key);
                throw new IllegalStateException("해당 요청이 현재 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }
            
            // 락 획득 후, 별도의 트랜잭션 컨텍스트에서 비즈니스 로직을 실행 (메서드 종료 시 즉시 커밋)
            return aopForTransaction.proceed(joinPoint);
            
        } catch (InterruptedException e) {
            log.error("Lock acquisition interrupted", e);
            Thread.currentThread().interrupt();
            throw new IllegalStateException("락 획득 중 인터럽트가 발생했습니다.");
        } finally {
            try {
                // 현재 스레드가 락을 보유하고 있을 때만 해제
                if (rLock != null && rLock.isHeldByCurrentThread()) {
                    rLock.unlock();
                    log.debug("Lock released [{}]", key);
                }
            } catch (IllegalMonitorStateException e) {
                log.warn("이미 해제된 락입니다. [{}]", key);
            }
        }
    }

    private String parseKey(String[] parameterNames, Object[] args, String key) {
        if (parameterNames == null || parameterNames.length == 0) {
            return key;
        }
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        Object value = parser.parseExpression(key).getValue(context);
        return value != null ? value.toString() : key;
    }
}
