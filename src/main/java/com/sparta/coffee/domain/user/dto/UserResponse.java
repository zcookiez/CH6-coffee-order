package com.sparta.coffee.domain.user.dto;

import com.sparta.coffee.domain.user.entity.User;
import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String name
) {
    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
