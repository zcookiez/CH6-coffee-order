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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.redisson.api.RedissonClient;

/**
 * Redis 포트를 고의로 존재하지 않는 포트(9999)로 설정하여
 * Redis 다운(장애) 상황을 완벽하게 시뮬레이션합니다.
 * */
@SpringBootTest(properties = {
    "spring.data.redis.port=9999",
    "spring.data.redis.connect-timeout=100", // 초기 커넥션 타임아웃 명시적 지정
    "spring.data.redis.timeout=100"
})
class CacheFallbackIntegrationTest {

    @MockBean
    private RedissonClient redissonClient; // Redisson 초기화 시 연결 시도

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
        menuDailyStatsRepository.deleteAll();
        menuRepository.deleteAll();
    }

    @Test
    @DisplayName("Redis 서버가 죽어있어도 앱이 터지지 않고 DB 조회(Fallback)를 통해 정상적으로 결과를 반환한다")
    void getPopularMenus_RedisFallback() {
        // given
        Menu americano = menuRepository.save(Menu.builder().name("아메리카노(대체)").price(4500).stock(100).status(MenuStatus.ON_SALE).build());
        Menu latte = menuRepository.save(Menu.builder().name("카페라떼(대체)").price(5000).stock(100).status(MenuStatus.ON_SALE).build());

        LocalDate yesterday = LocalDate.now().minusDays(1); // D-1

        // TransactionTemplate을 사용하여
        // Native Query(UPSERT)가 즉시 Flush 및 Commit 되도록 독립된 트랜잭션으로 묶어줍니다.
        transactionTemplate.executeWithoutResult(status -> {
            menuDailyStatsRepository.upsertOrderCount(americano.getId(), yesterday, 100);
            menuDailyStatsRepository.upsertOrderCount(latte.getId(), yesterday, 50);
        });

        // when
        // @Cacheable이 붙은 메서드를 호출합니다.
        // 포트가 9999이므로 Redis 접근에 무조건 실패하지만, ErrorHandler 덕분에 우회(Fallback)합니다.
        List<MenuResponse> popularMenus = menuService.getPopularMenus();

        // then
        // 에러(500 Internal Server Error)가 터지지 않고 DB에서 데이터를 무사히 퍼와야 성공!
        assertThat(popularMenus).isNotEmpty();
        assertThat(popularMenus.get(0).name()).isEqualTo("아메리카노(대체)");
        assertThat(popularMenus.get(1).name()).isEqualTo("카페라떼(대체)");
    }
}
