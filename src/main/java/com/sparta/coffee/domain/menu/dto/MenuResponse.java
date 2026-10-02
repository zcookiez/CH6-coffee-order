package com.sparta.coffee.domain.menu.dto;

import com.sparta.coffee.domain.menu.entity.Menu;
import com.sparta.coffee.domain.menu.entity.MenuStatus;

public record MenuResponse(
        Long id,
        String name,
        int price,
        int stock,
        MenuStatus status
) {
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getName(),
                menu.getPrice(),
                menu.getStock(),
                menu.getStatus()
        );
    }
}
