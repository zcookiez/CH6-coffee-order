package com.sparta.coffee.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * README.md에 정의된 공통 응답 규격 래퍼 클래스입니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, 200, "요청이 성공적으로 처리되었습니다.", data);
    }
}
