package com.sparta.coffee.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "사용자 회원가입 요청 DTO")
public record UserSignupRequest(
        @Schema(description = "사용자 이름", example = "홍길동")
        @NotBlank(message = "사용자 이름은 필수입니다.")
        String name
) {
}
