package com.sparta.coffee.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
@Schema(description = "커피 주문 요청 DTO")
public record OrderRequest(
        @Schema(description = "주문하는 사용자 ID", example = "1")
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @Schema(description = "주문할 메뉴 ID", example = "2")
        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId,

        @Schema(description = "주문 수량 (최소 1개 이상)", example = "3")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int quantity
) {
}
