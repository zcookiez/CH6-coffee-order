package com.sparta.coffee.domain.menu.dto;

import com.sparta.coffee.domain.menu.entity.Menu;
import com.sparta.coffee.domain.menu.entity.MenuStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커피 메뉴 응답 DTO")
public record MenuResponse(
        @Schema(description = "메뉴 ID", example = "1")
        Long id,

        @Schema(description = "메뉴명", example = "아메리카노")
        String name,

        @Schema(description = "가격", example = "4500")
        int price,

        @Schema(description = "남은 재고", example = "100")
        int stock,

        @Schema(description = "메뉴 판매 상태", example = "ON_SALE")
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
