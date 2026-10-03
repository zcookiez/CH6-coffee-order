package com.sparta.coffee.domain.user.controller;

import com.sparta.coffee.domain.user.dto.UserResponse;
import com.sparta.coffee.domain.user.dto.UserSignupRequest;
import com.sparta.coffee.domain.user.service.UserService;
import com.sparta.coffee.global.response.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "사용자 API", description = "사용자 회원가입 및 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 가입", description = "신규 사용자를 등록합니다.")
    @PostMapping("/signup")
    public CommonResponse<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserResponse response = userService.signup(request);
        return CommonResponse.success(response);
    }
}
