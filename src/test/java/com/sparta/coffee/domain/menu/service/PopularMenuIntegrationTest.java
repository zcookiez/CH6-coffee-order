package com.sparta.coffee.domain.menu.service;

import com.sparta.coffee.domain.menu.dto.MenuResponse;
import com.sparta.coffee.domain.menu.entity.Menu;
import com.sparta.coffee.domain.menu.entity.MenuStatus;
import com.sparta.coffee.domain.menu.repository.MenuDailyStatsRepository;
import com.sparta.coffee.domain.menu.repository.MenuRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PopularMenuIntegrationTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private MenuDailyStatsRepository menuDailyStatsRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @AfterEach
    void tearDown() {
        // 독립적인 트랜잭션으로 데이터를 넣었으므로, 여기서 명시적으로 삭제해 줍니다.
        menuDailyStatsRepository.deleteAll();
        menuRepository.deleteAll();
    }

    @Test
    @DisplayName("최근 7일간(어제 기준 과거 7일)의 통계를 기반으로 인기 메뉴 Top 3를 정확히 조회한다")
    void getPopularMenus() {
        // given
        Menu americano = menuRepository.save(Menu.builder().name("아메리카노").price(4500).stock(100).status(MenuStatus.ON_SALE).build());
        Menu latte = menuRepository.save(Menu.builder().name("카페라떼").price(5000).stock(100).status(MenuStatus.ON_SALE).build());
        Menu mocha = menuRepository.save(Menu.builder().name("카페모카").price(5500).stock(100).status(MenuStatus.ON_SALE).build());
        Menu espresso = menuRepository.save(Menu.builder().name("에스프레소").price(4000).stock(100).status(MenuStatus.ON_SALE).build());
        Menu tea = menuRepository.save(Menu.builder().name("녹차").price(6000).stock(100).status(MenuStatus.ON_SALE).build());

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1); // D-1
        LocalDate threeDaysAgo = today.minusDays(3);
        LocalDate sevenDaysAgo = today.minusDays(7); // D-7
        LocalDate eightDaysAgo = today.minusDays(8); // D-8 (집계 제외 대상)

        // Repository의 순수성을 유지하기 위해 테스트 클래스 레벨의 @Transactional을 제거하고,
        // TransactionTemplate을 사용하여 Native Query(UPSERT)가 즉시 Flush/Commit 되도록 강제합니다.
        // 이를 통해 뒤이어 실행되는 JPQL 조회가 영속성 컨텍스트를 거치지 않고 완벽하게 DB의 최신 상태를 읽을 수 있습니다.
        transactionTemplate.executeWithoutResult(status -> {
            menuDailyStatsRepository.upsertOrderCount(americano.getId(), yesterday, 10);
            menuDailyStatsRepository.upsertOrderCount(americano.getId(), threeDaysAgo, 20);

            menuDailyStatsRepository.upsertOrderCount(latte.getId(), sevenDaysAgo, 25);

            menuDailyStatsRepository.upsertOrderCount(espresso.getId(), threeDaysAgo, 5);
            menuDailyStatsRepository.upsertOrderCount(espresso.getId(), eightDaysAgo, 10);

            menuDailyStatsRepository.upsertOrderCount(mocha.getId(), today, 10);
        });

        // when
        // 자정 스케줄러가 호출하는 원본 로직(DB 쿼리 -> Redis 저장)을 직접 호출
        List<MenuResponse> popularMenus = menuService.warmUpPopularMenusCache();

        // then
        assertThat(popularMenus).hasSize(3);
        
        // 1위 검증: 아메리카노
        assertThat(popularMenus.get(0).id()).isEqualTo(americano.getId());
        assertThat(popularMenus.get(0).name()).isEqualTo("아메리카노");
        
        // 2위 검증: 카페라떼
        assertThat(popularMenus.get(1).id()).isEqualTo(latte.getId());
        
        // 3위 검증: 에스프레소 (8일 전 데이터는 제외되어 5잔으로 산정되므로 3위)
        assertThat(popularMenus.get(2).id()).isEqualTo(espresso.getId());
    }
}
