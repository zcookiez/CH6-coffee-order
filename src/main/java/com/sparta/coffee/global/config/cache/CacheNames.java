package com.sparta.coffee.global.config.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CacheNames {

    public static final String MENUS_ALL = "menus:all";
    public static final String POPULAR_MENUS = "popular:menus:top3";
}
