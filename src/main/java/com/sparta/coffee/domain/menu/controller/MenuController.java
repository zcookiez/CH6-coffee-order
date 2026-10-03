package com.sparta.coffee.domain.menu.controller;

import com.sparta.coffee.domain.menu.dto.MenuResponse;
import com.sparta.coffee.domain.menu.service.MenuService;
import com.sparta.coffee.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public CommonResponse<List<MenuResponse>> getAllMenus() {
        List<MenuResponse> menus = menuService.getAllMenus();
        return CommonResponse.success(menus);
    }

    @GetMapping("/popular")
    public CommonResponse<List<MenuResponse>> getPopularMenus() {
        List<MenuResponse> popularMenus = menuService.getPopularMenus();
        return CommonResponse.success(popularMenus);
    }
}
