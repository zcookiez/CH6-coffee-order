package com.sparta.coffee.domain.user.dto;

import com.sparta.coffee.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "사용자 응답 DTO")
public record UserResponse(
        @Schema(description = "사용자 ID", example = "1")
        Long id,
        
        @Schema(description = "사용자 이름", example = "홍길동")
        String name
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
