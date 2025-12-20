package com.edu.edu_note.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 직접 정의한 BusinessException 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        // ErrorCode에 정의된 그대로 반환
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    // 2. 예상치 못한 모든 서버 에러 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        // 500 Internal Server Error 로 통일
        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}


//package com.edu.edu_note.global.exception;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@Slf4j // 1. 로그 기능을 사용하기 위한 롬복 어노테이션
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    // 1. 직접 정의한 BusinessException 처리
//    @ExceptionHandler(BusinessException.class)
//    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
//        // 비즈니스 예외는 보통 '경고' 수준으로 로그를 남깁니다.
//        log.warn("BusinessException 발생: {}", e.getMessage());
//
//        // ErrorCode에 정의된 그대로 반환
//        return ErrorResponse.toResponseEntity(e.getErrorCode());
//    }
//
//    // 2. 예상치 못한 모든 서버 에러 처리 (여기가 핵심!)
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleException(Exception e) {
//        // 500 에러의 진짜 원인(Stack Trace)을 서버 로그에 붉게 출력합니다.
//        log.error("알 수 없는 서버 에러 발생(Unhandled Exception): ", e);
//
//        // 클라이언트에게는 "서버 내부 오류"라고만 알림
//        return ErrorResponse.toResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR);
//    }
//}