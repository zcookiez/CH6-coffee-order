package com.sparta.coffee.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserSignupRequest(
        @NotBlank(message = "사용자 이름은 필수입니다.")
        String name
) {
}
