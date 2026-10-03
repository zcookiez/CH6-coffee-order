package com.sparta.coffee.domain.menu.entity;

import com.sparta.coffee.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menu_daily_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"menu_id", "stat_date"})
})
public class MenuDailyStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "order_count", nullable = false)
    private long orderCount;

    @Builder
    public MenuDailyStats(Long menuId, LocalDate statDate, long orderCount) {
        this.menuId = menuId;
        this.statDate = statDate;
        this.orderCount = orderCount;
    }
}
