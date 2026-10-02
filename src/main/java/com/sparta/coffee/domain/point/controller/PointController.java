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

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/charge")
    public CommonResponse<PointResponse> chargePoint(@Valid @RequestBody PointChargeRequest request) {
        PointResponse response = pointService.chargePoint(request);
        return CommonResponse.success(response);
    }
}
