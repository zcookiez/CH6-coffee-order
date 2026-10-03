package com.sparta.coffee.domain.point.controller;

import com.sparta.coffee.domain.point.dto.PointChargeRequest;
import com.sparta.coffee.domain.point.dto.PointResponse;
import com.sparta.coffee.domain.point.service.PointService;
import com.sparta.coffee.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "포인트 API", description = "사용자 포인트 충전 및 관리 API")
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 충전", description = "사용자의 포인트를 충전합니다. (동시성 제어 적용)")
    @PostMapping("/charge")
    public CommonResponse<PointResponse> chargePoint(@Valid @RequestBody PointChargeRequest request) {
        PointResponse response = pointService.chargePoint(request);
        return CommonResponse.success(response);
    }
}
