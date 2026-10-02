package com.sparta.coffee.domain.menu.service;

import com.sparta.coffee.domain.menu.dto.MenuResponse;
import com.sparta.coffee.domain.menu.repository.MenuRepository;
import com.sparta.coffee.global.config.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

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
