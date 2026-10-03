package com.sparta.coffee.domain.menu.controller;

import com.sparta.coffee.domain.menu.dto.MenuResponse;
import com.sparta.coffee.domain.menu.service.MenuService;
import com.sparta.coffee.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "메뉴 API", description = "커피 메뉴 조회 및 인기 메뉴 랭킹 API")
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "전체 메뉴 조회", description = "현재 판매 중인 모든 커피 메뉴 목록을 조회합니다. (Redis 캐시 적용)")
    @GetMapping
    public CommonResponse<List<MenuResponse>> getAllMenus() {
        List<MenuResponse> menus = menuService.getAllMenus();
        return CommonResponse.success(menus);
    }

    @Operation(summary = "인기 메뉴 Top 3 조회", description = "최근 7일간 가장 많이 팔린 인기 메뉴 3개를 조회합니다. (캐시 및 장애 우회 적용)")
    @GetMapping("/popular")
    public CommonResponse<List<MenuResponse>> getPopularMenus() {
        List<MenuResponse> popularMenus = menuService.getPopularMenus();
        return CommonResponse.success(popularMenus);
    }
}
