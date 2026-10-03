package com.sparta.coffee.domain.menu.repository;

import com.sparta.coffee.domain.menu.entity.MenuDailyStats;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MenuDailyStatsRepository extends JpaRepository<MenuDailyStats, Long> {

    /**
     * JPA Rollback-Only 방어 및 원자적 동시성 제어를 위한 Native UPSERT 쿼리
     */
    @Modifying(clearAutomatically = true)
    @Query(value = "INSERT INTO menu_daily_stats (menu_id, stat_date, order_count, created_at, updated_at) " +
                   "VALUES (:menuId, :statDate, :quantity, NOW(), NOW()) " +
                   "ON DUPLICATE KEY UPDATE order_count = order_count + :quantity, updated_at = NOW()", 
           nativeQuery = true)
    void upsertOrderCount(@Param("menuId") Long menuId, @Param("statDate") LocalDate statDate, @Param("quantity") long quantity);

    @Query("SELECT m.menuId " +
           "FROM MenuDailyStats m " +
           "WHERE m.statDate >= :startDate AND m.statDate <= :endDate " +
           "GROUP BY m.menuId " +
           "ORDER BY SUM(m.orderCount) DESC")
    List<Long> findPopularMenuIds(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
}
