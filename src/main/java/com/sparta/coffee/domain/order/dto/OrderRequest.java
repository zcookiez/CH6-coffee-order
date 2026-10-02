package com.sparta.coffee.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId,

        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int quantity
) {
}
