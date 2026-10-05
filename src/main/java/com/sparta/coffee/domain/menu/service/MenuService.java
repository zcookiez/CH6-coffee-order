package com.sparta.coffee.domain.menu.service;

import com.sparta.coffee.domain.menu.dto.MenuResponse;
import com.sparta.coffee.domain.menu.repository.MenuDailyStatsRepository;
import com.sparta.coffee.domain.menu.repository.MenuRepository;
import com.sparta.coffee.global.config.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuDailyStatsRepository menuDailyStatsRepository;

    /**
     * 전체 커피 메뉴 목록 조회 (Cache-Aside 패턴 적용)
     * - Redis에 "menus:all" 키가 있으면 DB 조회 없이 캐시에서 즉시 반환
     * - 없으면 DB 조회 후 결과를 Redis에 저장
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.MENUS_ALL)
    public List<MenuResponse> getAllMenus() {
        return menuRepository.findAll().stream()
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * API 호출 시 동작하는 조회 로직 (Cache-Aside + Fallback)
     * Redis에 캐시가 없으면 DB에서 직접 통계를 계산(Fallback)하여 반환합니다.
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.POPULAR_MENUS, key = "'top3'")
    public List<MenuResponse> getPopularMenus() {
        return calculateTop3PopularMenus();
    }

    /**
     * 스케줄러가 호출하는 캐시 워밍 로직
     * DB에서 통계를 계산한 뒤 결과를 Redis에 덮어씌웁니다.
     */
    @Transactional(readOnly = true)
    @CachePut(cacheNames = CacheNames.POPULAR_MENUS, key = "'top3'")
    public List<MenuResponse> warmUpPopularMenusCache() {
        return calculateTop3PopularMenus();
    }

    private List<MenuResponse> calculateTop3PopularMenus() {
        // 자정 스케줄러 실행 시점 기준으로 '어제'부터 '7일 전'까지의 확정된 통계를 집계합니다.
        LocalDate endDate = LocalDate.now().minusDays(1); // D-1 (어제)
        LocalDate startDate = endDate.minusDays(6);       // D-7 (어제 기준 6일 전, 총 7일치)

        List<Long> topMenuIds = menuDailyStatsRepository.findPopularMenuIds(
                startDate, endDate, org.springframework.data.domain.PageRequest.of(0, 3)
        );

        if (topMenuIds.isEmpty()) {
            return List.of();
        }

        // 메뉴 ID 리스트를 기반으로 실제 메뉴 정보 조회 (통계 순위 순서 유지)
        List<com.sparta.coffee.domain.menu.entity.Menu> menus = menuRepository.findAllById(topMenuIds);
        
        return topMenuIds.stream()
                .map(id -> menus.stream().filter(m -> m.getId().equals(id)).findFirst().orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(MenuResponse::from)
                .collect(Collectors.toList());
    }

    /*
     * [TODO] 명시적 캐시 Evict (데이터 정합성 관리)
     * 현재는 RedisCacheConfig에 의해 1일 TTL(시간 만료)로 캐시가 자동 만료되지만,
     * 메뉴가 추가(Create), 수정(Update), 삭제(Delete)되어 데이터가 변경될 때는
     * DB와 캐시 간의 데이터 불일치가 발생하지 않도록 즉시 기존 캐시를 비워주어야 합니다.
     * 
     * 추후 메뉴 추가/수정/삭제 메서드를 구현할 때, 해당 메서드에 아래와 같이 @CacheEvict를 적용해야 합니다.
     * @CacheEvict(cacheNames = CacheNames.MENUS_ALL, allEntries = true)
     */
}
