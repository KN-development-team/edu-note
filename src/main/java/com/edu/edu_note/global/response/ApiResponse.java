package com.edu.edu_note.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

    private String message;     // "구글 로그인이 완료되었습니다."
    private T data;             // 실제 데이터 (없으면 null)
    private int statusCode;     // 200, 400, 404 등

    // 1. 데이터가 있는 성공 응답 (기본 200 OK)
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data, 200);
    }

    // 2. 데이터가 없는 성공 응답 (기본 200 OK)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(message, null, 200);
    }

    // 3. 에러 응답 등을 위해 코드를 직접 지정하고 싶을 때
    public static <T> ApiResponse<T> of(int statusCode, String message, T data) {
        return new ApiResponse<>(message, data, statusCode);
    }
}