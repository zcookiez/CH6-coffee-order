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

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public CommonResponse<UserResponse> signup(@Valid @RequestBody UserSignupRequest request) {
        UserResponse response = userService.signup(request);
        return CommonResponse.success(response);
    }
}
