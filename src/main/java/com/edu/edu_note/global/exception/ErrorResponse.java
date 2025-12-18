package com.edu.edu_note.global.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final String error;       // 예: "BadRequest", "Unauthorized"
    private final String message;     // 예: "인증코드가 필요합니다."
    private final int statusCode;     // 예: 400, 401

    // 에러를 반환할 때 편하게 쓰기 위한 정적 메서드
    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .error(errorCode.getName()) // ErrorCode에 name 필드 추가 예정
                        .message(errorCode.getMessage())
                        .statusCode(errorCode.getHttpStatus().value())
                        .build()
                );
    }
}