package com.sparta.coffee.domain.menu.scheduler;

import com.sparta.coffee.domain.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class PopularMenuScheduler {

    private final MenuService menuService;

    /**
     * 매일 자정(00:00:00)에 동작하는 인기 메뉴 캐시 워밍 스케줄러
     * - DB의 menu_daily_stats 테이블을 읽어 최신 Top 3를 계산한 뒤, Redis 캐시를 덮어씌웁니다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void warmUpPopularMenus() {
        log.info("인기 메뉴 캐시 워밍(Cache Warming) 스케줄러 시작...");
        try {
            menuService.warmUpPopularMenusCache();
            log.info("인기 메뉴 캐시 워밍 완료!");
        } catch (Exception e) {
            log.error("인기 메뉴 캐시 워밍 중 오류 발생", e);
        }
    }
}
