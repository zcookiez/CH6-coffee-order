package com.sparta.coffee.domain.point.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "포인트 충전 요청 DTO")
public record PointChargeRequest(
        @Schema(description = "충전할 사용자 ID", example = "1")
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @Schema(description = "충전 금액 (0 초과)", example = "10000")
        @Positive(message = "충전 금액은 0보다 커야 합니다.")
        Long amount,

        @Schema(description = "충전 요청 고유 식별자 (멱등성 보장용 UUID)", example = "req-uuid-1234")
        @NotNull(message = "충전 요청 고유 식별자(chargeRequestId)는 필수입니다.")
        String chargeRequestId
) {
}
