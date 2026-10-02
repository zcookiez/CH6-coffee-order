package com.sparta.coffee.domain.point.dto;

import com.sparta.coffee.domain.point.entity.Point;
import lombok.Builder;

@Builder
public record PointResponse(
        Long userId,
        Long balance
) {
    public static PointResponse from(Point point) {
        return PointResponse.builder()
                .userId(point.getUserId())
                .balance(point.getBalance())
                .build();
    }
}
