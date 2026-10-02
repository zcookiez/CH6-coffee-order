package com.sparta.coffee.domain.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointChargeRequest(
        @NotNull(message = "사용자 ID는 필수입니다.")
        Long userId,

        @Positive(message = "충전 금액은 0보다 커야 합니다.")
        Long amount,

        @NotNull(message = "충전 요청 고유 식별자(chargeRequestId)는 필수입니다.")
        String chargeRequestId
) {
}
